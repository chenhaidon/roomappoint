package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.GiftRedeemDto;
import com.example.web.dto.query.GiftRedeemPagedInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.BalanceRecord;
import com.example.web.entity.Gift;
import com.example.web.entity.GiftRedeem;
import com.example.web.entity.Integral;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.BalanceRecordMapper;
import com.example.web.mapper.GiftMapper;
import com.example.web.mapper.GiftRedeemMapper;
import com.example.web.mapper.IntegralMapper;
import com.example.web.service.GiftRedeemService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.CurrentUserDto;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class GiftRedeemServiceImpl extends ServiceImpl<GiftRedeemMapper, GiftRedeem> implements GiftRedeemService {

    @Autowired
    private GiftRedeemMapper _GiftRedeemMapper;

    @Autowired
    private GiftMapper _GiftMapper;

    @Autowired
    private IntegralMapper _IntegralMapper;

    @Autowired
    private AppUserMapper _AppUserMapper;

    @Autowired
    private BalanceRecordMapper _BalanceRecordMapper;

    private LambdaQueryWrapper<GiftRedeem> BuilderQuery(GiftRedeemPagedInput input) {
        LambdaQueryWrapper<GiftRedeem> queryWrapper = Wrappers.<GiftRedeem>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, GiftRedeem::getId, input.getId())
                .eq(input.getCreatorId() != null, GiftRedeem::getCreatorId, input.getCreatorId());

        if (input.getGiftId() != null) {
            queryWrapper = queryWrapper.eq(GiftRedeem::getGiftId, input.getGiftId());
        }
        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(GiftRedeem::getUserId, input.getUserId());
        }
        return queryWrapper;
    }

    private List<GiftRedeemDto> DispatchItem(List<GiftRedeemDto> items) throws InvocationTargetException, IllegalAccessException {
        for (GiftRedeemDto item : items) {
            Gift gift = _GiftMapper.selectById(item.getGiftId());
            if (gift != null) {
                item.setGiftDto(gift.MapToDto());
            }

            AppUser user = _AppUserMapper.selectById(item.getUserId());
            if (user != null) {
                item.setUserDto(user.MapToDto());
            }
        }
        return items;
    }

    @SneakyThrows
    @Override
    public PagedResult<GiftRedeemDto> List(GiftRedeemPagedInput input) {
        LambdaQueryWrapper<GiftRedeem> queryWrapper = BuilderQuery(input)
                .orderByDesc(GiftRedeem::getCreationTime);

        Page<GiftRedeem> page = new Page<>(input.getPage(), input.getLimit());
        IPage<GiftRedeem> pageRecords = _GiftRedeemMapper.selectPage(page, queryWrapper);
        Long totalCount = _GiftRedeemMapper.selectCount(queryWrapper);

        List<GiftRedeemDto> items = Extension.copyBeanList(pageRecords.getRecords(), GiftRedeemDto.class);
        items = DispatchItem(items);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public GiftRedeemDto Get(GiftRedeemPagedInput input) {
        if (input.getId() == null) {
            return new GiftRedeemDto();
        }
        PagedResult<GiftRedeemDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new GiftRedeemDto());
    }

    @Transactional
    @SneakyThrows
    @Override
    public GiftRedeemDto Redeem(IdInput input) {
        if (input.getId() == null) {
            throw new CustomException("GiftId不能为空");
        }

        CurrentUserDto currentUserDto = BaseContext.getCurrentUserDto();
        if (currentUserDto == null || currentUserDto.getUserId() == null) {
            throw new CustomException("未登录");
        }
        Integer userId = currentUserDto.getUserId();

        Gift gift = _GiftMapper.selectById(input.getId());
        if (gift == null) {
            throw new CustomException("礼品不存在");
        }
        if (!"上架".equals(gift.getStatus())) {
            throw new CustomException("礼品已下架");
        }
        if (gift.getNeedIntegral() == null || gift.getNeedIntegral() <= 0) {
            throw new CustomException("礼品积分配置不正确");
        }

        // 查询用户积分
        Integer totalIntegral = _IntegralMapper.selectList(Wrappers.<Integral>lambdaQuery().eq(Integral::getUserId, userId)).stream()
                .map(Integral::getIntegralValue)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        // 查询用户余额
        AppUser user = _AppUserMapper.selectById(userId);
        BigDecimal balance = (user != null && user.getBalance() != null) ? user.getBalance() : BigDecimal.ZERO;

        // 积分优先抵扣逻辑
        int needIntegral = gift.getNeedIntegral();
        int deductIntegral = Math.min(totalIntegral, needIntegral);
        BigDecimal deductBalance = BigDecimal.valueOf(needIntegral - deductIntegral);

        // 检查余额是否充足
        if (deductBalance.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(deductBalance) < 0) {
            throw new CustomException("积分和余额不足，需要" + needIntegral + "积分，你有" + totalIntegral + "积分和" + balance + "元余额");
        }

        // 扣库存
        int updated = _GiftMapper.update(null, new UpdateWrapper<Gift>()
                .setSql("Stock = Stock - 1")
                .eq("Id", gift.getId())
                .eq("Status", "上架")
                .gt("Stock", 0));
        if (updated <= 0) {
            throw new CustomException("库存不足");
        }

        // 创建兑换记录
        GiftRedeem redeem = new GiftRedeem();
        redeem.setGiftId(gift.getId());
        redeem.setUserId(userId);
        redeem.setNeedIntegral(gift.getNeedIntegral());
        redeem.setDeductIntegral(deductIntegral);
        redeem.setDeductBalance(deductBalance);
        redeem.setTitle("兑换：" + gift.getName());
        _GiftRedeemMapper.insert(redeem);

        // 扣积分
        if (deductIntegral > 0) {
            Integral integral = new Integral();
            integral.setIntegralValue(-deductIntegral);
            integral.setSource("礼品兑换");
            integral.setUserId(userId);
            integral.setTitle("兑换：" + gift.getName());
            integral.setRelativeCode(String.valueOf(redeem.getId()));
            _IntegralMapper.insert(integral);
        }

        // 扣余额
        if (deductBalance.compareTo(BigDecimal.ZERO) > 0) {
            _AppUserMapper.update(null, new UpdateWrapper<AppUser>()
                    .setSql("Balance = Balance - " + deductBalance)
                    .eq("Id", userId));

            BalanceRecord balanceRecord = new BalanceRecord();
            balanceRecord.setUserId(userId);
            balanceRecord.setAmount(deductBalance.negate());
            balanceRecord.setSource("礼品兑换");
            balanceRecord.setRelativeCode(String.valueOf(redeem.getId()));
            balanceRecord.setTitle("兑换：" + gift.getName());
            _BalanceRecordMapper.insert(balanceRecord);
        }

        return redeem.MapToDto();
    }

    @SneakyThrows
    @Override
    public PagedResult<GiftRedeemDto> MyList(GiftRedeemPagedInput input) {
        CurrentUserDto currentUserDto = BaseContext.getCurrentUserDto();
        if (currentUserDto == null || currentUserDto.getUserId() == null) {
            throw new CustomException("未登录");
        }

        if (input == null) {
            input = new GiftRedeemPagedInput();
        }
        input.setUserId(currentUserDto.getUserId());
        return List(input);
    }

    @Override
    public void Delete(IdInput input) {
        GiftRedeem entity = _GiftRedeemMapper.selectById(input.getId());
        _GiftRedeemMapper.deleteById(entity);
    }

    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }
}
