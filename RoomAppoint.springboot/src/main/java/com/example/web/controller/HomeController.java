package com.example.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.web.dto.*;
import com.example.web.dto.query.AnnouncementPagedInput;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.service.AnnouncementService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/Home")
public class HomeController {

    @Autowired()
    private AppUserMapper _AppUserMapper;
    @Autowired()
    private AppointRecordMapper _AppointRecordMapper;
    @Autowired()
    private RoomMapper _RoomMapper;
    @Autowired()
    private SeatMapper _SeatMapper;
    @Autowired()
    private AnnouncementService _AnnouncementService;

    @RequestMapping(value = "/GetHomeData", method = RequestMethod.POST)
    public HashMap<String, Object> GetHomeData() {
        Integer userId = BaseContext.getCurrentUserDto().getUserId();
        HashMap<String, Object> result = new HashMap<>();

        // 1. 用户信息
        AppUser user = _AppUserMapper.selectById(userId);
        BigDecimal balance = (user != null && user.getBalance() != null) ? user.getBalance() : BigDecimal.ZERO;
        result.put("Balance", balance);
        result.put("UserName", user != null ? (user.getName() != null ? user.getName() : user.getUserName()) : "");

        // 2. 今日时间范围
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 3. 查询当前用户今日所有预约记录
        LambdaQueryWrapper<AppointRecord> todayQuery = new LambdaQueryWrapper<AppointRecord>()
                .eq(AppointRecord::getUserId, userId)
                .ge(AppointRecord::getCreationTime, todayStart)
                .le(AppointRecord::getCreationTime, todayEnd);
        List<AppointRecord> todayRecords = _AppointRecordMapper.selectList(todayQuery);

        // 4. 计算今日学习总时长（分钟）
        long totalMinutes = 0;
        for (AppointRecord record : todayRecords) {
            if (record.getBeginTime() != null) {
                LocalDateTime end = (record.getEndTime() != null) ? record.getEndTime() : LocalDateTime.now();
                totalMinutes += ChronoUnit.MINUTES.between(record.getBeginTime(), end);
            }
        }
        result.put("TodayStudyMinutes", Math.max(0, totalMinutes));

        // 5. 当前预约状态（待使用=1 或 使用中=2）
        AppointRecord current = null;
        for (AppointRecord record : todayRecords) {
            if (record.getAppointStatus() != null && (record.getAppointStatus() == 1 || record.getAppointStatus() == 2)) {
                current = record;
                break;
            }
        }

        if (current != null) {
            result.put("AppointStatus", current.getAppointStatus());
            Room room = _RoomMapper.selectById(current.getRoomId());
            result.put("RoomName", room != null ? room.getName() : "");
            Seat seat = _SeatMapper.selectById(current.getSeatId());
            result.put("SeatName", seat != null ? seat.getNo() + "号座" : "");
            result.put("BeginTime", current.getBeginTime() != null ? current.getBeginTime().toString() : "");
        } else {
            result.put("AppointStatus", 0);
            result.put("RoomName", "");
            result.put("SeatName", "");
            result.put("BeginTime", "");
        }

        // 6. 最近使用的房间
        LambdaQueryWrapper<AppointRecord> recentQuery = new LambdaQueryWrapper<AppointRecord>()
                .eq(AppointRecord::getUserId, userId)
                .orderByDesc(AppointRecord::getCreationTime)
                .last("LIMIT 1");
        AppointRecord recentRecord = _AppointRecordMapper.selectOne(recentQuery);
        if (recentRecord != null && recentRecord.getRoomId() != null) {
            Room recentRoom = _RoomMapper.selectById(recentRecord.getRoomId());
            result.put("RecentRoomName", recentRoom != null ? recentRoom.getName() : "");
            result.put("RecentRoomId", recentRecord.getRoomId());
        } else {
            result.put("RecentRoomName", "");
            result.put("RecentRoomId", null);
        }

        // 7. 房间列表 + 空位统计 + 推荐
        List<Room> allRooms = _RoomMapper.selectList(null);
        List<Seat> allSeats = _SeatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getIsMaintain, false)
        );
        Set<Integer> inUseSeatIds = _AppointRecordMapper.selectList(
                new LambdaQueryWrapper<AppointRecord>()
                        .in(AppointRecord::getAppointStatus, 1, 2)
        ).stream().map(AppointRecord::getSeatId).collect(Collectors.toSet());

        Map<Integer, List<Seat>> seatsByRoom = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRoomId));

        List<HomeDataDto.RoomSummary> roomSummaries = new ArrayList<>();
        int totalSeats = 0;
        int totalInUse = 0;

        for (Room room : allRooms) {
            List<Seat> roomSeats = seatsByRoom.getOrDefault(room.getId(), Collections.emptyList());
            int roomTotal = roomSeats.size();
            int roomInUse = (int) roomSeats.stream()
                    .filter(s -> inUseSeatIds.contains(s.getId()))
                    .count();
            int available = roomTotal - roomInUse;

            HomeDataDto.RoomSummary summary = new HomeDataDto.RoomSummary();
            summary.setId(room.getId());
            summary.setName(room.getName());
            summary.setCover(room.getCover());
            summary.setTotalSeats(roomTotal);
            summary.setAvailableSeats(available);
            roomSummaries.add(summary);

            totalSeats += roomTotal;
            totalInUse += roomInUse;
        }

        // 推荐：空位最多的前3个
        List<HomeDataDto.RoomSummary> recommended = roomSummaries.stream()
                .filter(r -> r.getAvailableSeats() > 0)
                .sorted((a, b) -> b.getAvailableSeats() - a.getAvailableSeats())
                .limit(3)
                .collect(Collectors.toList());
        // 添加推荐标签
        for (int i = 0; i < recommended.size(); i++) {
            HomeDataDto.RoomSummary r = recommended.get(i);
            double ratio = r.getTotalSeats() > 0 ? (double) r.getAvailableSeats() / r.getTotalSeats() : 0;
            if (i == 0) {
                r.setRecommendTag("空位最多");
            } else if (ratio > 0.5) {
                r.setRecommendTag("空位较多");
            } else {
                r.setRecommendTag("环境安静");
            }
        }

        result.put("RecommendedRooms", recommended);
        result.put("Rooms", roomSummaries);
        result.put("TotalInUse", totalInUse);
        result.put("TotalAvailable", totalSeats - totalInUse);

        // 8. 最新公告（3条）
        AnnouncementPagedInput annoInput = new AnnouncementPagedInput();
        annoInput.setPage(1L);
        annoInput.setLimit(3L);
        PagedResult<AnnouncementDto> announcements = _AnnouncementService.FrontList(annoInput);
        result.put("Announcements", announcements.getItems());

        return result;
    }

    /**
     * 一键选座：自动选房间+选座位
     */
    @RequestMapping(value = "/AutoSelectSeat", method = RequestMethod.POST)
    public HashMap<String, Object> AutoSelectSeat() {
        HashMap<String, Object> result = new HashMap<>();

        // 找空位最多的房间
        List<Room> allRooms = _RoomMapper.selectList(null);
        List<Seat> allSeats = _SeatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getIsMaintain, false)
        );
        Set<Integer> inUseSeatIds = _AppointRecordMapper.selectList(
                new LambdaQueryWrapper<AppointRecord>()
                        .in(AppointRecord::getAppointStatus, 1, 2)
        ).stream().map(AppointRecord::getSeatId).collect(Collectors.toSet());

        Map<Integer, List<Seat>> seatsByRoom = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRoomId));

        Room bestRoom = null;
        Seat bestSeat = null;
        int maxAvailable = 0;

        for (Room room : allRooms) {
            List<Seat> roomSeats = seatsByRoom.getOrDefault(room.getId(), Collections.emptyList());
            List<Seat> available = roomSeats.stream()
                    .filter(s -> !inUseSeatIds.contains(s.getId()))
                    .collect(Collectors.toList());
            if (available.size() > maxAvailable) {
                maxAvailable = available.size();
                bestRoom = room;
                bestSeat = available.get(0);
            }
        }

        if (bestRoom != null && bestSeat != null) {
            result.put("Success", true);
            result.put("RoomId", bestRoom.getId());
            result.put("RoomName", bestRoom.getName());
            result.put("SeatId", bestSeat.getId());
            result.put("SeatName", bestSeat.getNo() + "号座");
        } else {
            result.put("Success", false);
            result.put("Msg", "暂无可用座位");
        }

        return result;
    }
}
