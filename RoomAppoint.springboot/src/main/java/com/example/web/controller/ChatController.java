package com.example.web.controller;

import com.example.web.dto.ChatRequestDto;
import com.example.web.entity.ChatHistory;
import com.example.web.service.ChatService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.CurrentUserDto;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping(value = "/Send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter Send(@RequestBody ChatRequestDto dto) {
        CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("请先登录");
        }
        if (!chatService.isEnabled()) {
            throw new CustomException("AI助手未启用");
        }
        return chatService.sendMessage(currentUser.getUserId(), dto);
    }

    @PostMapping("/History")
    public List<ChatHistory> History(@RequestBody Map<String, String> body) {
        CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("请先登录");
        }
        String conversationId = body.get("ConversationId");
        return chatService.getHistory(currentUser.getUserId(), conversationId);
    }

    @PostMapping("/CheckEnabled")
    public Map<String, Object> CheckEnabled() {
        Map<String, Object> result = new HashMap<>();
        result.put("Enabled", chatService.isEnabled());
        return result;
    }

    @PostMapping("/TestConnection")
    public Map<String, Object> TestConnection() {
        requireAdmin();
        return chatService.testConnection();
    }

    private void requireAdmin() {
        CurrentUserDto current = BaseContext.getCurrentUserDto();
        if (current == null || current.getRoleType() == null
                || current.getRoleType() != com.example.web.enums.RoleTypeEnum.管理员) {
            throw new CustomException("无权限");
        }
    }
}
