package com.example.web.dto;

import com.example.web.entity.Integral;
import com.example.web.entity.Gift;
import com.example.web.entity.GiftRedeem;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Data
public class GiftRedeemDto extends BaseDto {

    @JsonProperty("GiftId")
    private Integer GiftId;

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("NeedIntegral")
    private Integer NeedIntegral;

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("RedeemCode")
    private String RedeemCode;

    @JsonProperty("DeductIntegral")
    private Integer DeductIntegral;

    @JsonProperty("DeductBalance")
    private BigDecimal DeductBalance;

    @JsonProperty("GiftDto")
    private GiftDto GiftDto;

    @JsonProperty("UserDto")
    private AppUserDto UserDto;

    public GiftRedeem MapToEntity() throws InvocationTargetException, IllegalAccessException {
        GiftRedeem entity = new GiftRedeem();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
