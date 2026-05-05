package com.example.web.service;

import com.example.web.dto.ChatRequestDto;
import com.example.web.entity.ChatHistory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface ChatService {

    SseEmitter sendMessage(Integer userId, ChatRequestDto dto);

    List<ChatHistory> getHistory(Integer userId, String conversationId);

    boolean isEnabled();

    Map<String, Object> testConnection();
}
