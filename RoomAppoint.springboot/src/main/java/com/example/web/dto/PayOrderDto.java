package com.example.web.dto;

import com.example.web.entity.PayOrder;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayOrderDto extends BaseDto {

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("OrderNo")
    private String OrderNo;

    @JsonProperty("Amount")
    private BigDecimal Amount;

    @JsonProperty("Status")
    private String Status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("PayTime")
    private LocalDateTime PayTime;

    @JsonProperty("TradeNo")
    private String TradeNo;

    public PayOrder MapToEntity() throws InvocationTargetException, IllegalAccessException {
        PayOrder entity = new PayOrder();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
