package com.example.web.controller;

import com.example.web.dto.GiftRedeemDto;
import com.example.web.dto.query.GiftRedeemPagedInput;
import com.example.web.service.GiftRedeemService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/GiftRedeem")
public class GiftRedeemController {

    @Autowired
    private GiftRedeemService _GiftRedeemService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<GiftRedeemDto> List(@RequestBody GiftRedeemPagedInput input) {
        return _GiftRedeemService.List(input);
    }

    @RequestMapping(value = "/MyList", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<GiftRedeemDto> MyList(@RequestBody GiftRedeemPagedInput input) {
        return _GiftRedeemService.MyList(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public GiftRedeemDto Get(@RequestBody GiftRedeemPagedInput input) {
        return _GiftRedeemService.Get(input);
    }

    @RequestMapping(value = "/Redeem", method = RequestMethod.POST)
    @SneakyThrows
    public GiftRedeemDto Redeem(@RequestBody IdInput input) {
        return _GiftRedeemService.Redeem(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        _GiftRedeemService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        _GiftRedeemService.BatchDelete(input);
    }
}
