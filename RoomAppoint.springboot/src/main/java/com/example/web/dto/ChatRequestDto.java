package com.example.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatRequestDto {

    @JsonProperty("Message")
    private String Message;

    @JsonProperty("ConversationId")
    private String ConversationId;
}
