package com.example.web.dto;

import com.example.web.entity.Gift;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

@Data
public class GiftDto extends BaseDto {

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("Cover")
    private String Cover;

    @JsonProperty("NeedIntegral")
    private Integer NeedIntegral;

    @JsonProperty("Stock")
    private Integer Stock;

    @JsonProperty("Status")
    private String Status;

    @JsonProperty("Summary")
    private String Summary;

    @JsonProperty("Sort")
    private Integer Sort;

    public Gift MapToEntity() throws InvocationTargetException, IllegalAccessException {
        Gift entity = new Gift();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
