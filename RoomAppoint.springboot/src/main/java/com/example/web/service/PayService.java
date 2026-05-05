package com.example.web.service;

import com.example.web.dto.RechargeInput;

public interface PayService {

    String CreateOrder(RechargeInput input);

    String Notify(String params);

    void Return(String outTradeNo);
}
