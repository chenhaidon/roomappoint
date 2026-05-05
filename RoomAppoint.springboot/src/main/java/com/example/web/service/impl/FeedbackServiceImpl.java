package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.FeedbackDto;
import com.example.web.dto.query.FeedbackPagedInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.Feedback;
import com.example.web.enums.FeedbackStatusEnum;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.FeedbackMapper;
import com.example.web.service.FeedbackService;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Autowired
    private AppUserMapper _AppUserMapper;

    @Autowired
    private FeedbackMapper _FeedbackMapper;

    private LambdaQueryWrapper<Feedback> BuilderQuery(FeedbackPagedInput input) {
        LambdaQueryWrapper<Feedback> queryWrapper = Wrappers.<Feedback>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, Feedback::getId, input.getId())
                .eq(input.getCreatorId() != null, Feedback::getCreatorId, input.getCreatorId());

        if (Extension.isNotNullOrEmpty(input.getTitleLike())) {
            queryWrapper = queryWrapper.like(Feedback::getTitle, input.getTitleLike());
        }
        if (Extension.isNotNullOrEmpty(input.getContentLike())) {
            queryWrapper = queryWrapper.like(Feedback::getContent, input.getContentLike());
        }
        if (input.getStatus() != null) {
            queryWrapper = queryWrapper.eq(Feedback::getStatus, input.getStatus());
        }
        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(Feedback::getUserId, input.getUserId());
        }
        return queryWrapper;
    }

    private List<FeedbackDto> DispatchItem(List<FeedbackDto> items) throws InvocationTargetException, IllegalAccessException {
        for (FeedbackDto item : items) {
            AppUser user = _AppUserMapper.selectList(
                    Wrappers.<AppUser>lambdaQuery().eq(AppUser::getId, item.getUserId())
            ).stream().findFirst().orElse(new AppUser());
            item.setUserDto(user.MapToDto());

            AppUser creator = _AppUserMapper.selectList(
                    Wrappers.<AppUser>lambdaQuery().eq(AppUser::getId, item.getCreatorId())
            ).stream().findFirst().orElse(new AppUser());
            item.setCreatorAppUserDto(creator.MapToDto());
        }
        return items;
    }

    @SneakyThrows
    @Override
    public PagedResult<FeedbackDto> List(FeedbackPagedInput input) {
        LambdaQueryWrapper<Feedback> queryWrapper = BuilderQuery(input)
                .orderByDesc(Feedback::getCreationTime);

        Page<Feedback> page = new Page<>(input.getPage(), input.getLimit());
        IPage<Feedback> pageRecords = _FeedbackMapper.selectPage(page, queryWrapper);
        Long totalCount = _FeedbackMapper.selectCount(queryWrapper);

        List<FeedbackDto> items = Extension.copyBeanList(pageRecords.getRecords(), FeedbackDto.class);
        items = DispatchItem(items);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public FeedbackDto Get(FeedbackPagedInput input) {
        if (input.getId() == null) {
            return new FeedbackDto();
        }
        PagedResult<FeedbackDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new FeedbackDto());
    }

    @SneakyThrows
    @Override
    public FeedbackDto CreateOrEdit(FeedbackDto input) {
        Feedback entity = input.MapToEntity();
        if (entity.getStatus() == null) {
            entity.setStatus(FeedbackStatusEnum.待处理.index());
        }
        saveOrUpdate(entity);
        return entity.MapToDto();
    }

    @Override
    public void Delete(IdInput input) {
        Feedback entity = _FeedbackMapper.selectById(input.getId());
        _FeedbackMapper.deleteById(entity);
    }

    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }

    @SneakyThrows
    @Override
    public FeedbackDto Reply(FeedbackDto input) {
        Feedback entity = _FeedbackMapper.selectById(input.getId());
        entity.setReply(input.getReply());
        entity.setReplyTime(LocalDateTime.now());
        entity.setStatus(FeedbackStatusEnum.已处理.index());
        _FeedbackMapper.updateById(entity);
        return entity.MapToDto();
    }
}
