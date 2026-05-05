package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.FeedbackDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Data
@TableName("`Feedback`")
public class Feedback extends BaseEntity {

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.IGNORED)
    private Integer UserId;

    @JsonProperty("Title")
    @TableField(value = "Title", updateStrategy = FieldStrategy.IGNORED)
    private String Title;

    @JsonProperty("Content")
    @TableField(value = "Content", updateStrategy = FieldStrategy.IGNORED)
    private String Content;

    @JsonProperty("Status")
    @TableField(value = "Status", updateStrategy = FieldStrategy.IGNORED)
    private Integer Status;

    @JsonProperty("Reply")
    @TableField(value = "Reply", updateStrategy = FieldStrategy.IGNORED)
    private String Reply;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonProperty("ReplyTime")
    @TableField(value = "ReplyTime", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime ReplyTime;

    public FeedbackDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        FeedbackDto dto = new FeedbackDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
