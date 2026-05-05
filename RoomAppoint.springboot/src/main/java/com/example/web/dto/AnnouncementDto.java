package com.example.web.dto;

import com.example.web.entity.Announcement;
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
public class AnnouncementDto extends BaseDto {

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("Summary")
    private String Summary;

    @JsonProperty("Content")
    private String Content;

    @JsonProperty("Cover")
    private String Cover;

    @JsonProperty("Status")
    private String Status;

    @JsonProperty("IsTop")
    private Integer IsTop;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("PublishTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime PublishTime;

    public Announcement MapToEntity() throws InvocationTargetException, IllegalAccessException {
        Announcement entity = new Announcement();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
