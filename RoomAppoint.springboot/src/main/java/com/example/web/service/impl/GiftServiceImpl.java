package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.GiftDto;
import com.example.web.dto.query.GiftPagedInput;
import com.example.web.entity.Gift;
import com.example.web.mapper.GiftMapper;
import com.example.web.service.GiftService;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GiftServiceImpl extends ServiceImpl<GiftMapper, Gift> implements GiftService {

    @Autowired
    private GiftMapper _GiftMapper;

    private LambdaQueryWrapper<Gift> BuilderQuery(GiftPagedInput input) {
        LambdaQueryWrapper<Gift> queryWrapper = Wrappers.<Gift>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, Gift::getId, input.getId())
                .eq(input.getCreatorId() != null, Gift::getCreatorId, input.getCreatorId());

        if (Extension.isNotNullOrEmpty(input.getNameLike())) {
            queryWrapper = queryWrapper.like(Gift::getName, input.getNameLike());
        }
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            queryWrapper = queryWrapper.eq(Gift::getStatus, input.getStatus());
        }
        return queryWrapper;
    }

    @SneakyThrows
    @Override
    public PagedResult<GiftDto> List(GiftPagedInput input) {
        LambdaQueryWrapper<Gift> queryWrapper = BuilderQuery(input)
                .orderByDesc(Gift::getSort)
                .orderByDesc(Gift::getCreationTime);

        Page<Gift> page = new Page<>(input.getPage(), input.getLimit());
        IPage<Gift> pageRecords = _GiftMapper.selectPage(page, queryWrapper);
        Long totalCount = _GiftMapper.selectCount(queryWrapper);

        List<GiftDto> items = Extension.copyBeanList(pageRecords.getRecords(), GiftDto.class);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public PagedResult<GiftDto> FrontList(GiftPagedInput input) {
        if (Extension.isNullOrEmpty(input.getStatus())) {
            input.setStatus("上架");
        }

        LambdaQueryWrapper<Gift> queryWrapper = BuilderQuery(input)
                .gt(Gift::getStock, 0)
                .orderByDesc(Gift::getSort)
                .orderByDesc(Gift::getCreationTime);

        Page<Gift> page = new Page<>(input.getPage(), input.getLimit());
        IPage<Gift> pageRecords = _GiftMapper.selectPage(page, queryWrapper);
        Long totalCount = _GiftMapper.selectCount(queryWrapper);

        List<GiftDto> items = Extension.copyBeanList(pageRecords.getRecords(), GiftDto.class);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public GiftDto Get(GiftPagedInput input) {
        if (input.getId() == null) {
            return new GiftDto();
        }
        PagedResult<GiftDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new GiftDto());
    }

    @SneakyThrows
    @Override
    public GiftDto CreateOrEdit(GiftDto input) {
        Gift entity = input.MapToEntity();

        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (Extension.isNullOrEmpty(entity.getStatus())) {
            entity.setStatus("上架");
        }
        if (entity.getStock() == null) {
            entity.setStock(0);
        }

        saveOrUpdate(entity);
        return entity.MapToDto();
    }

    @Override
    public void Delete(IdInput input) {
        Gift entity = _GiftMapper.selectById(input.getId());
        _GiftMapper.deleteById(entity);
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
