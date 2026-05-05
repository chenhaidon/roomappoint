package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.GiftRedeemDto;
import com.example.web.dto.query.GiftRedeemPagedInput;
import com.example.web.entity.GiftRedeem;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

public interface GiftRedeemService extends IService<GiftRedeem> {

    PagedResult<GiftRedeemDto> List(GiftRedeemPagedInput input);

    GiftRedeemDto Get(GiftRedeemPagedInput input);

    GiftRedeemDto Redeem(IdInput input);

    PagedResult<GiftRedeemDto> MyList(GiftRedeemPagedInput input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);
}
