package com.example.web.controller;

import com.example.web.dto.RechargeInput;
import com.example.web.service.PayService;
import com.example.web.tools.dto.ResponseData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;

@RestController()
@RequestMapping("/Pay")
public class PayController {

    @Autowired()
    private PayService _PayService;

    /**
     * 创建充值订单，返回支付宝支付信息
     */
    @RequestMapping(value = "/CreateOrder", method = RequestMethod.POST)
    @SneakyThrows
    public ResponseData<String> CreateOrder(@RequestBody RechargeInput input) {
        String result = _PayService.CreateOrder(input);
        return ResponseData.GetResponseDataInstance(result, "成功", true);
    }

    /**
     * 支付宝异步回调 — 需要直接返回纯文本 "success"
     */
    @RequestMapping(value = "/Notify", method = RequestMethod.POST)
    public void Notify(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            writePlain(response, "fail");
            return;
        }
        String result = _PayService.Notify(sb.toString());
        writePlain(response, result);
    }

    /**
     * 支付宝同步跳转
     */
    @RequestMapping(value = "/Return", method = RequestMethod.GET)
    public void Return(@RequestParam(value = "out_trade_no", required = false) String outTradeNo,
                       HttpServletResponse response) {
        _PayService.Return(outTradeNo);
        response.setContentType("text/html;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write("<!DOCTYPE html><html><body style='text-align:center;padding-top:100px;font-family:sans-serif'>" +
                    "<h2>支付成功！</h2><p>2秒后自动跳转...</p>" +
                    "<script>setTimeout(function(){window.location.href='/#/Front/BalanceRecordList'},2000)</script>" +
                    "</body></html>");
            writer.flush();
        } catch (Exception e) {
            // ignore
        }
    }

    private void writePlain(HttpServletResponse response, String text) {
        response.setContentType("text/plain;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write(text);
            writer.flush();
        } catch (Exception e) {
            // ignore
        }
    }
}
