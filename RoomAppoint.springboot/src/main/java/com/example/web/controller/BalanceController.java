package com.example.web.controller;

import com.example.web.dto.*;
import com.example.web.dto.query.BalanceRecordPagedInput;
import com.example.web.service.BalanceService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/Balance")
public class BalanceController {

    @Autowired()
    private BalanceService _BalanceService;

    @RequestMapping(value = "/RecordList", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<BalanceRecordDto> RecordList(@RequestBody BalanceRecordPagedInput input) {
        return _BalanceService.List(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public BalanceRecordDto Get(@RequestBody BalanceRecordPagedInput input) {
        return _BalanceService.Get(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public BalanceRecordDto CreateOrEdit(@RequestBody BalanceRecordDto input) throws Exception {
        return _BalanceService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        _BalanceService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        _BalanceService.BatchDelete(input);
    }

    @RequestMapping(value = "/GetMyBalance", method = RequestMethod.POST)
    @SneakyThrows
    public MyBalanceDataDto GetMyBalance() {
        return _BalanceService.GetMyBalance();
    }

    @RequestMapping(value = "/AdminAdjust", method = RequestMethod.POST)
    @SneakyThrows
    public void AdminAdjust(@RequestBody RechargeInput input) {
        _BalanceService.AdminAdjust(input);
    }
}
