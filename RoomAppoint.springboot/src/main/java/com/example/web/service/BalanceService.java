package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.query.BalanceRecordPagedInput;
import com.example.web.entity.BalanceRecord;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

public interface BalanceService extends IService<BalanceRecord> {

    PagedResult<BalanceRecordDto> List(BalanceRecordPagedInput input);

    BalanceRecordDto Get(BalanceRecordPagedInput input);

    BalanceRecordDto CreateOrEdit(BalanceRecordDto input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);

    MyBalanceDataDto GetMyBalance();

    void AdminAdjust(RechargeInput input);
}
