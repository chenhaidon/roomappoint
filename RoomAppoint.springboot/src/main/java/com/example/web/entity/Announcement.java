package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.AnnouncementDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Data
@TableName("`Announcement`")
public class Announcement extends BaseEntity {

    @JsonProperty("Title")
    @TableField(value = "Title", updateStrategy = FieldStrategy.IGNORED)
    private String Title;

    @JsonProperty("Summary")
    @TableField(value = "Summary", updateStrategy = FieldStrategy.IGNORED)
    private String Summary;

    @JsonProperty("Content")
    @TableField(value = "Content", updateStrategy = FieldStrategy.IGNORED)
    private String Content;

    @JsonProperty("Cover")
    @TableField(value = "Cover", updateStrategy = FieldStrategy.IGNORED)
    private String Cover;

    @JsonProperty("Status")
    @TableField(value = "Status", updateStrategy = FieldStrategy.IGNORED)
    private String Status;

    @JsonProperty("IsTop")
    @TableField(value = "IsTop", updateStrategy = FieldStrategy.IGNORED)
    private Integer IsTop;

    @JsonProperty("PublishTime")
    @TableField(value = "PublishTime", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime PublishTime;

    public AnnouncementDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        AnnouncementDto dto = new AnnouncementDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
