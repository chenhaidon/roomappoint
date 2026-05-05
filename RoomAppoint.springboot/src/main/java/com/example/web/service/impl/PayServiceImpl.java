package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.RechargeInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.BalanceRecord;
import com.example.web.entity.PayOrder;
import com.example.web.entity.SystemConfig;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.BalanceRecordMapper;
import com.example.web.mapper.PayOrderMapper;
import com.example.web.mapper.SystemConfigMapper;
import com.example.web.service.PayService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.CurrentUserDto;
import com.example.web.tools.exception.CustomException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private PayOrderMapper _PayOrderMapper;

    @Autowired
    private AppUserMapper _AppUserMapper;

    @Autowired
    private BalanceRecordMapper _BalanceRecordMapper;

    @Autowired
    private SystemConfigMapper _SystemConfigMapper;

    private String getConfig(String key) {
        SystemConfig config = _SystemConfigMapper.selectOne(
                Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : "";
    }

    private String generateOrderNo() {
        return "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    @Override
    public String CreateOrder(RechargeInput input) {
        CurrentUserDto currentUserDto = BaseContext.getCurrentUserDto();
        if (currentUserDto == null || currentUserDto.getUserId() == null) {
            throw new CustomException("未登录");
        }

        if (input.getAmount() == null || input.getAmount().compareTo(BigDecimal.ONE) < 0) {
            throw new CustomException("充值金额不能小于1元");
        }

        // 创建支付订单
        PayOrder order = new PayOrder();
        order.setUserId(currentUserDto.getUserId());
        order.setOrderNo(generateOrderNo());
        order.setAmount(input.getAmount());
        order.setStatus("待支付");
        _PayOrderMapper.insert(order);

        // 读取支付宝配置
        String appId = getConfig("alipay.appid");
        String privateKey = getConfig("alipay.privateKey");
        String alipayPublicKey = getConfig("alipay.alipayPublicKey");
        String gateway = getConfig("alipay.gateway");
        String notifyUrl = getConfig("alipay.notifyUrl");
        String returnUrl = getConfig("alipay.returnUrl");

        if (appId.isEmpty() || privateKey.isEmpty()) {
            throw new CustomException("支付宝配置不完整，请联系管理员");
        }

        // 使用支付宝 SDK 生成签名表单
        AlipayClient alipayClient = new DefaultAlipayClient(gateway, appId, privateKey, "json", "utf-8", alipayPublicKey, "RSA2");
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(notifyUrl);
        payRequest.setReturnUrl(returnUrl);

        String bizContent = "{\"out_trade_no\":\"" + order.getOrderNo() + "\","
                + "\"total_amount\":\"" + order.getAmount().toPlainString() + "\","
                + "\"subject\":\"自习室余额充值\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        payRequest.setBizContent(bizContent);

        try {
            String formHtml = alipayClient.pageExecute(payRequest).getBody();
            log.info("支付宝表单生成成功，订单号：{}", order.getOrderNo());
            return formHtml;
        } catch (AlipayApiException e) {
            log.error("支付宝表单生成失败", e);
            throw new CustomException("支付宝下单失败：" + e.getMessage());
        }
    }

    @Transactional
    @Override
    public String Notify(String params) {
        // 支付宝异步回调处理
        // 解析回调参数
        Map<String, String> paramsMap = parseAlipayParams(params);

        String tradeStatus = paramsMap.get("trade_status");
        String outTradeNo = paramsMap.get("out_trade_no");
        String tradeNo = paramsMap.get("trade_no");
        String totalAmount = paramsMap.get("total_amount");

        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "fail";
        }

        // 查询订单
        PayOrder order = _PayOrderMapper.selectOne(
                Wrappers.<PayOrder>lambdaQuery().eq(PayOrder::getOrderNo, outTradeNo));
        if (order == null) {
            log.warn("支付宝回调：订单不存在 {}", outTradeNo);
            return "fail";
        }

        if ("已支付".equals(order.getStatus())) {
            return "success"; // 幂等处理
        }

        // 验证金额
        if (order.getAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
            log.warn("支付宝回调：金额不匹配 订单{} 回调{}", order.getAmount(), totalAmount);
            return "fail";
        }

        // 更新订单状态
        _PayOrderMapper.update(null, new UpdateWrapper<PayOrder>()
                .set("Status", "已支付")
                .set("PayTime", LocalDateTime.now())
                .set("TradeNo", tradeNo)
                .eq("OrderNo", outTradeNo));

        // 更新用户余额
        _AppUserMapper.update(null, new UpdateWrapper<AppUser>()
                .setSql("Balance = Balance + " + order.getAmount())
                .eq("Id", order.getUserId()));

        // 记录余额流水
        BalanceRecord record = new BalanceRecord();
        record.setUserId(order.getUserId());
        record.setAmount(order.getAmount());
        record.setSource("充值");
        record.setRelativeCode(outTradeNo);
        record.setTitle("支付宝充值 " + order.getAmount() + "元");
        _BalanceRecordMapper.insert(record);

        log.info("充值成功：用户{} 金额{} 订单{}", order.getUserId(), order.getAmount(), outTradeNo);
        return "success";
    }

    @Override
    public void Return(String outTradeNo) {
        // 同步跳转，可以做额外处理（目前直接返回即可）
        log.info("支付同步返回：{}", outTradeNo);
    }

    private Map<String, String> parseAlipayParams(String params) {
        Map<String, String> map = new HashMap<>();
        if (params == null || params.isEmpty()) return map;
        String[] pairs = params.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                map.put(key, value);
            }
        }
        return map;
    }
}
