package com.example.web.dto;

import com.example.web.entity.Feedback;
import com.example.web.enums.FeedbackStatusEnum;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class FeedbackDto extends BaseDto {

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("Content")
    private String Content;

    @JsonProperty("Status")
    private Integer Status;

    @JsonProperty("Reply")
    private String Reply;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ReplyTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime ReplyTime;

    @JsonProperty("UserDto")
    private AppUserDto UserDto;

    @JsonProperty("StatusFormat")
    public String getStatusFormat() {
        return FeedbackStatusEnum.GetEnum(Status).toString();
    }

    public Feedback MapToEntity() throws InvocationTargetException, IllegalAccessException {
        Feedback entity = new Feedback();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
