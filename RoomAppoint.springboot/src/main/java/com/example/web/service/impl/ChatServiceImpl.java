package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.ChatRequestDto;
import com.example.web.entity.*;
import com.example.web.enums.AppointDateTypeEnum;
import com.example.web.enums.AppointStatusEnum;
import com.example.web.mapper.*;
import com.example.web.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private ChatHistoryMapper chatHistoryMapper;
    @Autowired
    private AppUserMapper appUserMapper;
    @Autowired
    private IntegralMapper integralMapper;
    @Autowired
    private AppointRecordMapper appointRecordMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private SeatMapper seatMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isEnabled() {
        return "1".equals(getConfigValue("ai.enabled"));
    }

    @Override
    public List<ChatHistory> getHistory(Integer userId, String conversationId) {
        LambdaQueryWrapper<ChatHistory> query = Wrappers.<ChatHistory>lambdaQuery()
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getConversationId, conversationId)
                .orderByAsc(ChatHistory::getCreationTime)
                .last("LIMIT 50");
        return chatHistoryMapper.selectList(query);
    }

    @Override
    public SseEmitter sendMessage(Integer userId, ChatRequestDto dto) {
        SseEmitter emitter = new SseEmitter(120_000L);

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Save user message
                ChatHistory userMsg = new ChatHistory();
                userMsg.setUserId(userId);
                userMsg.setRole("user");
                userMsg.setContent(dto.getMessage());
                userMsg.setConversationId(dto.getConversationId());
                userMsg.setCreationTime(LocalDateTime.now());
                chatHistoryMapper.insert(userMsg);

                // 2. Get conversation history
                LambdaQueryWrapper<ChatHistory> query = Wrappers.<ChatHistory>lambdaQuery()
                        .eq(ChatHistory::getUserId, userId)
                        .eq(ChatHistory::getConversationId, dto.getConversationId())
                        .orderByDesc(ChatHistory::getCreationTime)
                        .last("LIMIT 20");
                List<ChatHistory> history = chatHistoryMapper.selectList(query);
                Collections.reverse(history);

                // 3. Build request
                String apiBase = getConfigValue("ai.api_url");
                String apiKey = getConfigValue("ai.api_key");
                String model = getConfigValue("ai.model");

                String apiUrl = apiBase;
                if (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
                if (!apiUrl.endsWith("/v1/messages")) apiUrl = apiUrl + "/v1/messages";

                String systemPrompt = buildSystemPrompt(userId);

                ArrayNode messages = objectMapper.createArrayNode();
                for (ChatHistory h : history) {
                    ObjectNode msg = objectMapper.createObjectNode();
                    msg.put("role", h.getRole());
                    msg.put("content", h.getContent());
                    messages.add(msg);
                }

                ArrayNode tools = buildTools();

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10)).build();

                // 4. Tool use loop (max 5 rounds)
                StringBuilder fullResponse = new StringBuilder();
                for (int round = 0; round < 5; round++) {
                    ObjectNode requestBody = objectMapper.createObjectNode();
                    requestBody.put("model", model);
                    requestBody.put("max_tokens", 1024);
                    requestBody.put("system", systemPrompt);
                    requestBody.set("messages", messages);
                    requestBody.set("tools", tools);
                    requestBody.put("stream", false);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("Content-Type", "application/json")
                            .header("x-api-key", apiKey)
                            .header("anthropic-version", "2023-06-01")
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                            .timeout(Duration.ofSeconds(120)).build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        emitter.send(SseEmitter.event()
                                .data("{\"error\":\"API请求失败: HTTP " + response.statusCode() + "\"}"));
                        emitter.complete();
                        return;
                    }

                    JsonNode respJson = objectMapper.readTree(response.body());
                    String stopReason = respJson.path("stop_reason").asText("");
                    JsonNode content = respJson.get("content");

                    // Check for tool_use blocks
                    if ("tool_use".equals(stopReason)) {
                        // Add assistant message with tool_use blocks
                        ObjectNode assistantMsg = objectMapper.createObjectNode();
                        assistantMsg.put("role", "assistant");
                        assistantMsg.set("content", content);
                        messages.add(assistantMsg);

                        // Execute each tool and collect results
                        ArrayNode toolResults = objectMapper.createArrayNode();
                        for (JsonNode block : content) {
                            if ("tool_use".equals(block.path("type").asText())) {
                                String toolId = block.get("id").asText();
                                String toolName = block.get("name").asText();
                                JsonNode toolInput = block.get("input");

                                // Notify frontend: tool is executing
                                emitter.send(SseEmitter.event().data(
                                        objectMapper.writeValueAsString(
                                                objectMapper.createObjectNode()
                                                        .put("tool", toolName)
                                                        .put("status", "executing"))));

                                String toolResult = executeTool(userId, toolName, toolInput);

                                // Notify frontend: tool result
                                emitter.send(SseEmitter.event().data(
                                        objectMapper.writeValueAsString(
                                                objectMapper.createObjectNode()
                                                        .put("tool", toolName)
                                                        .put("status", "done")
                                                        .put("result", toolResult))));

                                ObjectNode resultBlock = objectMapper.createObjectNode();
                                resultBlock.put("type", "tool_result");
                                resultBlock.put("tool_use_id", toolId);
                                resultBlock.put("content", toolResult);
                                toolResults.add(resultBlock);
                            }
                        }

                        // Add tool result message
                        ObjectNode toolResultMsg = objectMapper.createObjectNode();
                        toolResultMsg.put("role", "user");
                        toolResultMsg.set("content", toolResults);
                        messages.add(toolResultMsg);

                        // Continue loop for next round
                    } else {
                        // Final text response
                        if (content != null && content.isArray()) {
                            for (JsonNode block : content) {
                                if ("text".equals(block.path("type").asText())) {
                                    String text = block.get("text").asText();
                                    fullResponse.append(text);
                                }
                            }
                        }
                        break;
                    }
                }

                // 5. Stream final text to frontend
                String finalText = fullResponse.toString();
                // Send in chunks for streaming effect
                int chunkSize = 4;
                for (int i = 0; i < finalText.length(); i += chunkSize) {
                    String chunk = finalText.substring(i, Math.min(i + chunkSize, finalText.length()));
                    emitter.send(SseEmitter.event().data(
                            objectMapper.writeValueAsString(
                                    objectMapper.createObjectNode().put("content", chunk))));
                }
                emitter.send(SseEmitter.event().data("[DONE]"));

                // 6. Save assistant message
                ChatHistory assistantMsg = new ChatHistory();
                assistantMsg.setUserId(userId);
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(finalText);
                assistantMsg.setConversationId(dto.getConversationId());
                assistantMsg.setCreationTime(LocalDateTime.now());
                chatHistoryMapper.insert(assistantMsg);

                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .data("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}"));
                    emitter.complete();
                } catch (Exception ignored) {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }

    private ArrayNode buildTools() {
        ArrayNode tools = objectMapper.createArrayNode();

        // query_rooms
        tools.add(buildTool("query_rooms", "查询所有可用的自习室列表", objectMapper.createObjectNode()));

        // query_seats
        ObjectNode seatsInput = objectMapper.createObjectNode();
        ObjectNode seatsProps = objectMapper.createObjectNode();
        seatsProps.put("type", "integer");
        seatsProps.put("description", "自习室ID");
        ObjectNode seatsProperties = objectMapper.createObjectNode();
        seatsProperties.set("room_id", seatsProps);
        seatsInput.put("type", "object");
        seatsInput.set("properties", seatsProperties);
        seatsInput.set("required", buildArray("room_id"));
        tools.add(buildTool("query_seats", "查询指定自习室的座位列表及可用状态", seatsInput));

        // query_my_appointments
        tools.add(buildTool("query_my_appointments", "查询当前用户的预约记录", objectMapper.createObjectNode()));

        // query_integral
        tools.add(buildTool("query_integral", "查询当前用户的积分余额", objectMapper.createObjectNode()));

        // create_appointment
        ObjectNode createInput = objectMapper.createObjectNode();
        ObjectNode createProps = objectMapper.createObjectNode();

        ObjectNode roomIdProp = objectMapper.createObjectNode();
        roomIdProp.put("type", "integer");
        roomIdProp.put("description", "自习室ID");
        createProps.set("room_id", roomIdProp);

        ObjectNode seatIdProp = objectMapper.createObjectNode();
        seatIdProp.put("type", "integer");
        seatIdProp.put("description", "座位ID");
        createProps.set("seat_id", seatIdProp);

        ObjectNode dateProp = objectMapper.createObjectNode();
        dateProp.put("type", "string");
        dateProp.put("description", "预约日期，格式 yyyy-MM-dd");
        createProps.set("appoint_date", dateProp);

        ObjectNode dateTypeProp = objectMapper.createObjectNode();
        dateTypeProp.put("type", "integer");
        dateTypeProp.put("description", "时间段：1=上午(6:00-12:00)，2=下午(14:00-18:00)，3=夜晚(19:00-23:00)");
        createProps.set("appoint_date_type", dateTypeProp);

        createInput.put("type", "object");
        createInput.set("properties", createProps);
        createInput.set("required", buildArray("room_id", "seat_id", "appoint_date", "appoint_date_type"));
        tools.add(buildTool("create_appointment", "为用户创建自习室座位预约", createInput));

        // cancel_appointment
        ObjectNode cancelInput = objectMapper.createObjectNode();
        ObjectNode cancelProps = objectMapper.createObjectNode();
        ObjectNode appointIdProp = objectMapper.createObjectNode();
        appointIdProp.put("type", "integer");
        appointIdProp.put("description", "预约记录ID");
        cancelProps.set("appoint_id", appointIdProp);
        cancelInput.put("type", "object");
        cancelInput.set("properties", cancelProps);
        cancelInput.set("required", buildArray("appoint_id"));
        tools.add(buildTool("cancel_appointment", "取消一个预约", cancelInput));

        return tools;
    }

    private ObjectNode buildTool(String name, String description, ObjectNode inputSchema) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        tool.set("input_schema", inputSchema);
        return tool;
    }

    private ArrayNode buildArray(String... items) {
        ArrayNode arr = objectMapper.createArrayNode();
        for (String item : items) arr.add(item);
        return arr;
    }

    private String executeTool(Integer userId, String toolName, JsonNode input) {
        try {
            switch (toolName) {
                case "query_rooms":
                    return toolQueryRooms();
                case "query_seats":
                    return toolQuerySeats(input.get("room_id").asInt());
                case "query_my_appointments":
                    return toolQueryMyAppointments(userId);
                case "query_integral":
                    return toolQueryIntegral(userId);
                case "create_appointment":
                    return toolCreateAppointment(userId, input);
                case "cancel_appointment":
                    return toolCancelAppointment(userId, input.get("appoint_id").asInt());
                default:
                    return "未知工具: " + toolName;
            }
        } catch (Exception e) {
            return "执行失败: " + e.getMessage();
        }
    }

    private String toolQueryRooms() {
        List<Room> rooms = roomMapper.selectList(null);
        if (rooms.isEmpty()) return "暂无自习室";
        StringBuilder sb = new StringBuilder();
        for (Room r : rooms) {
            long seatCount = seatMapper.selectCount(
                    Wrappers.<Seat>lambdaQuery().eq(Seat::getRoomId, r.getId()));
            sb.append("ID:").append(r.getId())
                    .append(" 名称:").append(r.getName())
                    .append(" 地址:").append(r.getAddress() != null ? r.getAddress() : "无")
                    .append(" 座位数:").append(seatCount)
                    .append("\n");
        }
        return sb.toString();
    }

    private String toolQuerySeats(int roomId) {
        List<Seat> seats = seatMapper.selectList(
                Wrappers.<Seat>lambdaQuery().eq(Seat::getRoomId, roomId));
        if (seats.isEmpty()) return "该自习室暂无座位";

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(23, 59, 59);

        List<AppointRecord> todayRecords = appointRecordMapper.selectList(
                Wrappers.<AppointRecord>lambdaQuery()
                        .eq(AppointRecord::getRoomId, roomId)
                        .eq(AppointRecord::getAppointDate, dayStart)
                        .in(AppointRecord::getAppointStatus,
                                AppointStatusEnum.待使用.index(), AppointStatusEnum.使用中.index()));

        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) {
            if (Boolean.TRUE.equals(s.getIsMaintain())) continue;
            // Check availability for each time slot
            boolean amFree = todayRecords.stream().noneMatch(r ->
                    r.getSeatId().equals(s.getId()) && r.getAppointDateType() == AppointDateTypeEnum.上午.index());
            boolean pmFree = todayRecords.stream().noneMatch(r ->
                    r.getSeatId().equals(s.getId()) && r.getAppointDateType() == AppointDateTypeEnum.下午.index());
            boolean nmFree = todayRecords.stream().noneMatch(r ->
                    r.getSeatId().equals(s.getId()) && r.getAppointDateType() == AppointDateTypeEnum.夜晚.index());

            sb.append("座位ID:").append(s.getId())
                    .append(" 编号:").append(s.getNo())
                    .append(" 今日可用:上午").append(amFree ? "空闲" : "已占")
                    .append(" 下午").append(pmFree ? "空闲" : "已占")
                    .append(" 夜晚").append(nmFree ? "空闲" : "已占")
                    .append("\n");
        }
        return sb.toString();
    }

    private String toolQueryMyAppointments(Integer userId) {
        List<AppointRecord> records = appointRecordMapper.selectList(
                Wrappers.<AppointRecord>lambdaQuery()
                        .eq(AppointRecord::getUserId, userId)
                        .orderByDesc(AppointRecord::getCreationTime)
                        .last("LIMIT 10"));
        if (records.isEmpty()) return "暂无预约记录";

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (AppointRecord r : records) {
            Room room = roomMapper.selectById(r.getRoomId());
            Seat seat = seatMapper.selectById(r.getSeatId());
            String roomName = room != null ? room.getName() : "自习室#" + r.getRoomId();
            String seatNo = seat != null ? seat.getNo() : "座位#" + r.getSeatId();
            String date = r.getAppointDate() != null ? r.getAppointDate().format(fmt) : "未知";
            String timeType = AppointDateTypeEnum.GetEnum(r.getAppointDateType()).toString();
            String status = AppointStatusEnum.GetEnum(r.getAppointStatus()).toString();
            sb.append("ID:").append(r.getId())
                    .append(" ").append(roomName).append(" ").append(seatNo)
                    .append(" ").append(date).append(" ").append(timeType)
                    .append(" 状态:").append(status)
                    .append("\n");
        }
        return sb.toString();
    }

    private String toolQueryIntegral(Integer userId) {
        LambdaQueryWrapper<Integral> query = Wrappers.<Integral>lambdaQuery()
                .eq(Integral::getUserId, userId);
        List<Integral> integrals = integralMapper.selectList(query);
        int balance = integrals.stream().mapToInt(Integral::getIntegralValue).sum();
        return "当前积分余额: " + balance;
    }

    private String toolCreateAppointment(Integer userId, JsonNode input) {
        int roomId = input.get("room_id").asInt();
        int seatId = input.get("seat_id").asInt();
        String dateStr = input.get("appoint_date").asText();
        int dateType = input.get("appoint_date_type").asInt();

        // Validate room exists
        Room room = roomMapper.selectById(roomId);
        if (room == null) return "自习室不存在(ID:" + roomId + ")";

        // Validate seat exists
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || !seat.getRoomId().equals(roomId)) return "座位不存在或不属于该自习室(ID:" + seatId + ")";

        // Get user info
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) return "用户不存在";

        // Build appointment
        AppointRecord record = new AppointRecord();
        record.setRoomId(roomId);
        record.setSeatId(seatId);
        record.setUserId(userId);
        record.setPhone(user.getPhoneNumber());
        record.setName(user.getName() != null ? user.getName() : user.getUserName());
        record.setAppointDate(LocalDate.parse(dateStr).atStartOfDay());
        record.setAppointDateType(dateType);
        record.setAppointStatus(AppointStatusEnum.待使用.index());

        // Generate order number
        String no = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + new Random().nextInt(10000);
        record.setNo("D" + no);

        // Validation (same as AppointRecordServiceImpl.CheckIsAbleAppoint)
        // Check time hasn't passed
        if (dateType == AppointDateTypeEnum.上午.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(12).plusMinutes(-30)))
                return "预约时间已过，请重新选择";
        } else if (dateType == AppointDateTypeEnum.下午.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(18).plusMinutes(-30)))
                return "预约时间已过，请重新选择";
        } else if (dateType == AppointDateTypeEnum.夜晚.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(22).plusMinutes(-30)))
                return "预约时间已过，请重新选择";
        }

        // Check seat not taken
        Long taken = appointRecordMapper.selectCount(
                Wrappers.<AppointRecord>lambdaQuery()
                        .eq(AppointRecord::getRoomId, roomId)
                        .eq(AppointRecord::getSeatId, seatId)
                        .eq(AppointRecord::getAppointDate, record.getAppointDate())
                        .eq(AppointRecord::getAppointDateType, dateType)
                        .in(AppointRecord::getAppointStatus,
                                AppointStatusEnum.待使用.index(), AppointStatusEnum.使用中.index()));
        if (taken > 0) return "该时间段该座位已被预约，请选择其他座位";

        // Check user not already booked in this time slot
        Long userTaken = appointRecordMapper.selectCount(
                Wrappers.<AppointRecord>lambdaQuery()
                        .eq(AppointRecord::getUserId, userId)
                        .eq(AppointRecord::getAppointDate, record.getAppointDate())
                        .eq(AppointRecord::getAppointDateType, dateType)
                        .in(AppointRecord::getAppointStatus,
                                AppointStatusEnum.待使用.index(), AppointStatusEnum.使用中.index(),
                                AppointStatusEnum.待评论.index(), AppointStatusEnum.预约完成.index()));
        if (userTaken > 0) return "您已预约了该时间段的座位，不能重复预约";

        // Check overdue cancellation limit
        Integer cancelCount = user.getOverdueTimes();
        if (cancelCount != null && room.getEveryMonCancelCount() != null) {
            if (room.getEveryMonCancelCount() <= cancelCount) {
                return "您的逾期取消次数已达上限（" + cancelCount + "/" + room.getEveryMonCancelCount() + "），无法预约该自习室";
            }
        }

        // Save
        record.setCreationTime(LocalDateTime.now());
        record.setCreatorId(userId);
        appointRecordMapper.insert(record);

        String timeTypeName = AppointDateTypeEnum.GetEnum(dateType).toString();
        return "预约成功！订单号:" + record.getNo() + " " + room.getName() + " "
                + seat.getNo() + "号座 " + dateStr + " " + timeTypeName;
    }

    private String toolCancelAppointment(Integer userId, int appointId) {
        AppointRecord record = appointRecordMapper.selectById(appointId);
        if (record == null) return "预约记录不存在(ID:" + appointId + ")";
        if (!record.getUserId().equals(userId)) return "无权操作他人的预约";

        int currentStatus = record.getAppointStatus();
        if (currentStatus != AppointStatusEnum.待使用.index()) {
            return "当前状态为" + AppointStatusEnum.GetEnum(currentStatus) + "，只能取消待使用的预约";
        }

        // Check if overdue cancel
        record.setAppointStatus(AppointStatusEnum.用户取消.index());
        if (record.getAppointDateType() == AppointDateTypeEnum.上午.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(6))) {
                record.setAppointStatus(AppointStatusEnum.逾期取消.index());
            }
        } else if (record.getAppointDateType() == AppointDateTypeEnum.下午.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(18))) {
                record.setAppointStatus(AppointStatusEnum.逾期取消.index());
            }
        } else if (record.getAppointDateType() == AppointDateTypeEnum.夜晚.index()) {
            if (LocalDateTime.now().isAfter(record.getAppointDate().plusHours(23))) {
                record.setAppointStatus(AppointStatusEnum.逾期取消.index());
            }
        }

        if (record.getAppointStatus() == AppointStatusEnum.逾期取消.index()) {
            AppUser user = appUserMapper.selectById(userId);
            if (user.getOverdueTimes() == null) user.setOverdueTimes(0);
            user.setOverdueTimes(user.getOverdueTimes() + 1);
            appUserMapper.updateById(user);
        }

        appointRecordMapper.updateById(record);
        String statusName = AppointStatusEnum.GetEnum(record.getAppointStatus()).toString();
        if (record.getAppointStatus() == AppointStatusEnum.逾期取消.index()) {
            return "取消成功，状态变更为: " + statusName + "。此次为逾期取消，已累计1次逾期记录";
        }
        return "取消成功，状态变更为: " + statusName + "。此次为正常取消，不影响预约权限";
    }

    private String buildSystemPrompt(Integer userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是自习室预约系统的智能助手。你可以帮助用户查询信息和执行操作（预约、取消预约等）。\n");
        sb.append("当前时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("规则：\n");
        sb.append("- 只回答与自习室系统相关的问题\n");
        sb.append("- 回答简洁友好，使用中文\n");
        sb.append("- 不确定的信息不要编造\n");
        sb.append("- 当用户询问无关问题时，礼貌拒绝并引导回自习室话题\n");
        sb.append("- 执行预约前，先用工具查询可用座位，确认有空位后再创建\n");
        sb.append("- 创建预约需要用户确认：先告诉用户你要预约哪个座位/时间段，请用户确认后再执行\n");
        sb.append("- 取消预约规则：正常取消（用户主动取消未过期的预约）没有任何限制，可以随时取消；只有逾期取消（预约时间已过但未使用）才会累计次数，当逾期次数达到自习室设定的上限时才无法再预约该自习室\n");
        sb.append("- 回答取消相关问题时，必须明确区分【正常取消】和【逾期取消】，不要混淆\n");

        try {
            AppUser user = appUserMapper.selectById(userId);
            if (user != null) {
                sb.append("\n当前用户信息：\n");
                sb.append("- 姓名：").append(user.getName() != null ? user.getName() : user.getUserName()).append("\n");
                sb.append("- 手机：").append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "未绑定").append("\n");

                LambdaQueryWrapper<Integral> integralQuery = Wrappers.<Integral>lambdaQuery()
                        .eq(Integral::getUserId, userId);
                List<Integral> integrals = integralMapper.selectList(integralQuery);
                int balance = integrals.stream().mapToInt(Integral::getIntegralValue).sum();
                sb.append("- 积分余额：").append(balance).append("\n");

                if (user.getOverdueTimes() != null && user.getOverdueTimes() > 0) {
                    sb.append("- 逾期取消次数：").append(user.getOverdueTimes()).append("（仅逾期取消累计，正常取消不累计。当此次数达到自习室设定上限时将无法预约）\n");
                }
            }

            List<Room> rooms = roomMapper.selectList(null);
            if (!rooms.isEmpty()) {
                sb.append("\n可用自习室：\n");
                for (Room r : rooms) {
                    sb.append("  ID:").append(r.getId()).append(" ").append(r.getName());
                    if (r.getAddress() != null) sb.append("（").append(r.getAddress()).append("）");
                    if (r.getEveryMonCancelCount() != null) sb.append(" 逾期取消上限").append(r.getEveryMonCancelCount()).append("次（正常取消不计入）");
                    sb.append("\n");
                }
            }
        } catch (Exception e) { }

        return sb.toString();
    }

    private String getAppointStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待使用";
            case 2: return "使用中";
            case 3: return "预约完成";
            case 4: return "用户取消";
            case 5: return "逾期取消";
            case 6: return "待评论";
            default: return "未知";
        }
    }

    private String getConfigValue(String key) {
        LambdaQueryWrapper<SystemConfig> query = Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(query);
        return config != null ? config.getConfigValue() : "";
    }

    @Override
    public java.util.Map<String, Object> testConnection() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        String apiBase = getConfigValue("ai.api_url");
        String apiKey = getConfigValue("ai.api_key");
        String model = getConfigValue("ai.model");

        if (apiBase == null || apiBase.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            result.put("Success", false);
            result.put("Msg", "请先填写接口地址和API密钥并保存");
            return result;
        }

        String apiUrl = apiBase;
        if (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        if (!apiUrl.endsWith("/v1/messages")) apiUrl = apiUrl + "/v1/messages";

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 32);
            requestBody.put("system", "你是一个测试助手，请回复\"连接成功\"两个字。");
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", "测试");
            messages.add(userMsg);
            requestBody.set("messages", messages);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10)).build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                String text = "";
                JsonNode content = json.get("content");
                if (content != null && content.isArray() && content.size() > 0) {
                    text = content.get(0).path("text").asText("");
                }
                result.put("Success", true);
                result.put("Msg", "连接成功！模型回复：" + text);
            } else {
                result.put("Success", false);
                result.put("Msg", "连接失败，HTTP " + response.statusCode() + "：" + response.body());
            }
        } catch (java.net.ConnectException e) {
            result.put("Success", false);
            result.put("Msg", "连接超时，请检查接口地址是否正确");
        } catch (Exception e) {
            result.put("Success", false);
            result.put("Msg", "连接异常：" + e.getMessage());
        }

        return result;
    }
}
