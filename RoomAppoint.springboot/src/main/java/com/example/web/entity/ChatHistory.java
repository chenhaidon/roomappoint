package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName("`chathistory`")
public class ChatHistory extends BaseEntity {

    @JsonProperty("UserId")
    @TableField(value = "UserId")
    private Integer UserId;

    @JsonProperty("Role")
    @TableField(value = "Role")
    private String Role;

    @JsonProperty("Content")
    @TableField(value = "Content", updateStrategy = FieldStrategy.IGNORED)
    private String Content;

    @JsonProperty("ConversationId")
    @TableField(value = "ConversationId")
    private String ConversationId;
}
