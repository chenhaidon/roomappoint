package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.BalanceRecordDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Data
@TableName("`balance_record`")
public class BalanceRecord extends BaseEntity {

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.IGNORED)
    private Integer UserId;

    @JsonProperty("Amount")
    @TableField(value = "Amount", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal Amount;

    @JsonProperty("Source")
    @TableField(value = "Source", updateStrategy = FieldStrategy.IGNORED)
    private String Source;

    @JsonProperty("RelativeCode")
    @TableField(value = "RelativeCode", updateStrategy = FieldStrategy.IGNORED)
    private String RelativeCode;

    @JsonProperty("Title")
    @TableField(value = "Title", updateStrategy = FieldStrategy.IGNORED)
    private String Title;

    public BalanceRecordDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        BalanceRecordDto dto = new BalanceRecordDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
