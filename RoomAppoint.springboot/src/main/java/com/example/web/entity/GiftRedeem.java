package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.web.dto.GiftRedeemDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Data
@TableName("`GiftRedeem`")
public class GiftRedeem extends BaseEntity {

    @JsonProperty("GiftId")
    @TableField(value = "GiftId", updateStrategy = FieldStrategy.IGNORED)
    private Integer GiftId;

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.IGNORED)
    private Integer UserId;

    @JsonProperty("NeedIntegral")
    @TableField(value = "NeedIntegral", updateStrategy = FieldStrategy.IGNORED)
    private Integer NeedIntegral;

    @JsonProperty("Title")
    @TableField(value = "Title", updateStrategy = FieldStrategy.IGNORED)
    private String Title;

    @JsonProperty("RedeemCode")
    @TableField(value = "RedeemCode", updateStrategy = FieldStrategy.IGNORED)
    private String RedeemCode;

    @JsonProperty("DeductIntegral")
    @TableField(value = "DeductIntegral", updateStrategy = FieldStrategy.IGNORED)
    private Integer DeductIntegral;

    @JsonProperty("DeductBalance")
    @TableField(value = "DeductBalance", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal DeductBalance;

    public GiftRedeemDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        GiftRedeemDto dto = new GiftRedeemDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
