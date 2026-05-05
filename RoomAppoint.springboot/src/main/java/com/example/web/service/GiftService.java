package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.GiftDto;
import com.example.web.dto.query.GiftPagedInput;
import com.example.web.entity.Gift;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

public interface GiftService extends IService<Gift> {

    PagedResult<GiftDto> List(GiftPagedInput input);

    PagedResult<GiftDto> FrontList(GiftPagedInput input);

    GiftDto CreateOrEdit(GiftDto input);

    GiftDto Get(GiftPagedInput input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);
}
