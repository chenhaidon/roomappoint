package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.AnnouncementDto;
import com.example.web.dto.query.AnnouncementPagedInput;
import com.example.web.entity.Announcement;
import com.example.web.entity.AppUser;
import com.example.web.mapper.AnnouncementMapper;
import com.example.web.mapper.AppUserMapper;
import com.example.web.service.AnnouncementService;
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
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Autowired
    private AppUserMapper _AppUserMapper;

    @Autowired
    private AnnouncementMapper _AnnouncementMapper;

    private LambdaQueryWrapper<Announcement> BuilderQuery(AnnouncementPagedInput input) {
        LambdaQueryWrapper<Announcement> queryWrapper = Wrappers.<Announcement>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, Announcement::getId, input.getId())
                .eq(input.getCreatorId() != null, Announcement::getCreatorId, input.getCreatorId());

        if (Extension.isNotNullOrEmpty(input.getTitleLike())) {
            queryWrapper = queryWrapper.like(Announcement::getTitle, input.getTitleLike());
        }
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            queryWrapper = queryWrapper.eq(Announcement::getStatus, input.getStatus());
        }
        return queryWrapper;
    }

    private List<AnnouncementDto> DispatchItem(List<AnnouncementDto> items) throws InvocationTargetException, IllegalAccessException {
        for (AnnouncementDto item : items) {
            AppUser creator = _AppUserMapper.selectList(
                    Wrappers.<AppUser>lambdaQuery().eq(AppUser::getId, item.getCreatorId())
            ).stream().findFirst().orElse(new AppUser());
            item.setCreatorAppUserDto(creator.MapToDto());
        }
        return items;
    }

    @SneakyThrows
    @Override
    public PagedResult<AnnouncementDto> List(AnnouncementPagedInput input) {
        LambdaQueryWrapper<Announcement> queryWrapper = BuilderQuery(input)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreationTime);

        Page<Announcement> page = new Page<>(input.getPage(), input.getLimit());
        IPage<Announcement> pageRecords = _AnnouncementMapper.selectPage(page, queryWrapper);
        Long totalCount = _AnnouncementMapper.selectCount(queryWrapper);

        List<AnnouncementDto> items = Extension.copyBeanList(pageRecords.getRecords(), AnnouncementDto.class);
        items = DispatchItem(items);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public PagedResult<AnnouncementDto> FrontList(AnnouncementPagedInput input) {
        if (Extension.isNullOrEmpty(input.getStatus())) {
            input.setStatus("已发布");
        }
        return List(input);
    }

    @SneakyThrows
    @Override
    public AnnouncementDto FrontGet(AnnouncementPagedInput input) {
        if (input.getId() == null) {
            return new AnnouncementDto();
        }
        if (Extension.isNullOrEmpty(input.getStatus())) {
            input.setStatus("已发布");
        }
        return Get(input);
    }

    @SneakyThrows
    @Override
    public AnnouncementDto Get(AnnouncementPagedInput input) {
        if (input.getId() == null) {
            return new AnnouncementDto();
        }
        PagedResult<AnnouncementDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new AnnouncementDto());
    }

    @SneakyThrows
    @Override
    public AnnouncementDto CreateOrEdit(AnnouncementDto input) {
        Announcement entity = input.MapToEntity();
        if (entity.getIsTop() == null) {
            entity.setIsTop(0);
        }
        if ("已发布".equals(entity.getStatus()) && entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
        saveOrUpdate(entity);
        return entity.MapToDto();
    }

    @Override
    public void Delete(IdInput input) {
        Announcement entity = _AnnouncementMapper.selectById(input.getId());
        _AnnouncementMapper.deleteById(entity);
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
