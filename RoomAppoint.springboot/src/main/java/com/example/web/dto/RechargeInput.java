package com.example.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeInput {

    @JsonProperty("Amount")
    private BigDecimal Amount;
}
