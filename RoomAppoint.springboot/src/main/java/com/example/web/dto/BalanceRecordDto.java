package com.example.web.dto;

import com.example.web.entity.BalanceRecord;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Data
public class BalanceRecordDto extends BaseDto {

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("Amount")
    private BigDecimal Amount;

    @JsonProperty("Source")
    private String Source;

    @JsonProperty("RelativeCode")
    private String RelativeCode;

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("UserDto")
    private AppUserDto UserDto;

    public BalanceRecord MapToEntity() throws InvocationTargetException, IllegalAccessException {
        BalanceRecord entity = new BalanceRecord();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
