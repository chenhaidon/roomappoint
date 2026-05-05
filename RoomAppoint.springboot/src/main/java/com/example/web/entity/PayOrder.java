package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.PayOrderDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`pay_order`")
public class PayOrder extends BaseEntity {

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.IGNORED)
    private Integer UserId;

    @JsonProperty("OrderNo")
    @TableField(value = "OrderNo", updateStrategy = FieldStrategy.IGNORED)
    private String OrderNo;

    @JsonProperty("Amount")
    @TableField(value = "Amount", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal Amount;

    @JsonProperty("Status")
    @TableField(value = "Status", updateStrategy = FieldStrategy.IGNORED)
    private String Status;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonProperty("PayTime")
    @TableField(value = "PayTime", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime PayTime;

    @JsonProperty("TradeNo")
    @TableField(value = "TradeNo", updateStrategy = FieldStrategy.IGNORED)
    private String TradeNo;

    public PayOrderDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        PayOrderDto dto = new PayOrderDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
