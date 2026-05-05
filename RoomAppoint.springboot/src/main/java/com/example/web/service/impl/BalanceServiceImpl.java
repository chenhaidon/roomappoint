package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.BalanceRecordDto;
import com.example.web.dto.MyBalanceDataDto;
import com.example.web.dto.RechargeInput;
import com.example.web.dto.query.BalanceRecordPagedInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.BalanceRecord;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.BalanceRecordMapper;
import com.example.web.service.BalanceService;
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
import java.util.List;

@Service
public class BalanceServiceImpl extends ServiceImpl<BalanceRecordMapper, BalanceRecord> implements BalanceService {

    @Autowired
    private BalanceRecordMapper _BalanceRecordMapper;

    @Autowired
    private AppUserMapper _AppUserMapper;

    private LambdaQueryWrapper<BalanceRecord> BuilderQuery(BalanceRecordPagedInput input) {
        LambdaQueryWrapper<BalanceRecord> queryWrapper = Wrappers.<BalanceRecord>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, BalanceRecord::getId, input.getId())
                .eq(input.getCreatorId() != null, BalanceRecord::getCreatorId, input.getCreatorId());

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(BalanceRecord::getUserId, input.getUserId());
        }
        if (Extension.isNotNullOrEmpty(input.getSourceLike())) {
            queryWrapper = queryWrapper.like(BalanceRecord::getSource, input.getSourceLike());
        }
        if (Extension.isNotNullOrEmpty(input.getTitleLike())) {
            queryWrapper = queryWrapper.like(BalanceRecord::getTitle, input.getTitleLike());
        }
        return queryWrapper;
    }

    private List<BalanceRecordDto> DispatchItem(List<BalanceRecordDto> items) throws InvocationTargetException, IllegalAccessException {
        for (BalanceRecordDto item : items) {
            AppUser user = _AppUserMapper.selectById(item.getUserId());
            if (user != null) {
                item.setUserDto(user.MapToDto());
            }
        }
        return items;
    }

    @SneakyThrows
    @Override
    public PagedResult<BalanceRecordDto> List(BalanceRecordPagedInput input) {
        LambdaQueryWrapper<BalanceRecord> queryWrapper = BuilderQuery(input)
                .orderByDesc(BalanceRecord::getCreationTime);

        Page<BalanceRecord> page = new Page<>(input.getPage(), input.getLimit());
        IPage<BalanceRecord> pageRecords = _BalanceRecordMapper.selectPage(page, queryWrapper);
        Long totalCount = _BalanceRecordMapper.selectCount(queryWrapper);

        List<BalanceRecordDto> items = Extension.copyBeanList(pageRecords.getRecords(), BalanceRecordDto.class);
        items = DispatchItem(items);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public BalanceRecordDto Get(BalanceRecordPagedInput input) {
        if (input.getId() == null) {
            return new BalanceRecordDto();
        }
        PagedResult<BalanceRecordDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new BalanceRecordDto());
    }

    @SneakyThrows
    @Override
    public BalanceRecordDto CreateOrEdit(BalanceRecordDto input) {
        BalanceRecord entity = input.MapToEntity();
        saveOrUpdate(entity);
        return entity.MapToDto();
    }

    @Override
    public void Delete(IdInput input) {
        BalanceRecord entity = _BalanceRecordMapper.selectById(input.getId());
        _BalanceRecordMapper.deleteById(entity);
    }

    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }

    @Override
    public MyBalanceDataDto GetMyBalance() {
        CurrentUserDto currentUserDto = BaseContext.getCurrentUserDto();
        if (currentUserDto == null || currentUserDto.getUserId() == null) {
            throw new CustomException("未登录");
        }
        AppUser user = _AppUserMapper.selectById(currentUserDto.getUserId());
        MyBalanceDataDto dto = new MyBalanceDataDto();
        dto.setBalance(user != null && user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        return dto;
    }

    @Transactional
    @Override
    public void AdminAdjust(RechargeInput input) {
        CurrentUserDto currentUserDto = BaseContext.getCurrentUserDto();
        if (currentUserDto == null || currentUserDto.getUserId() == null) {
            throw new CustomException("未登录");
        }
        if (input.getAmount() == null || input.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new CustomException("调整金额不能为0");
        }

        // 管理员调整只支持增加余额（正数）
        Integer userId = currentUserDto.getUserId();
        AppUser user = _AppUserMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 更新余额
        _AppUserMapper.update(null, new UpdateWrapper<AppUser>()
                .setSql("Balance = Balance + " + input.getAmount())
                .eq("Id", userId));

        // 记录流水
        BalanceRecord record = new BalanceRecord();
        record.setUserId(userId);
        record.setAmount(input.getAmount());
        record.setSource("管理员调整");
        record.setTitle("管理员调整余额 +" + input.getAmount());
        _BalanceRecordMapper.insert(record);
    }
}
