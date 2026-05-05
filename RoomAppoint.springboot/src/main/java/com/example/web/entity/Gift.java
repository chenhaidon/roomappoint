package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.GiftDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

@Data
@TableName("`Gift`")
public class Gift extends BaseEntity {

    @JsonProperty("Name")
    @TableField(value = "Name", updateStrategy = FieldStrategy.IGNORED)
    private String Name;

    @JsonProperty("Cover")
    @TableField(value = "Cover", updateStrategy = FieldStrategy.IGNORED)
    private String Cover;

    @JsonProperty("NeedIntegral")
    @TableField(value = "NeedIntegral", updateStrategy = FieldStrategy.IGNORED)
    private Integer NeedIntegral;

    @JsonProperty("Stock")
    @TableField(value = "Stock", updateStrategy = FieldStrategy.IGNORED)
    private Integer Stock;

    @JsonProperty("Status")
    @TableField(value = "Status", updateStrategy = FieldStrategy.IGNORED)
    private String Status;

    @JsonProperty("Summary")
    @TableField(value = "Summary", updateStrategy = FieldStrategy.IGNORED)
    private String Summary;

    @JsonProperty("Sort")
    @TableField(value = "Sort", updateStrategy = FieldStrategy.IGNORED)
    private Integer Sort;

    public GiftDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        GiftDto dto = new GiftDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
