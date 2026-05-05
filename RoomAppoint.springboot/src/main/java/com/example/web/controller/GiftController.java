package com.example.web.controller;

import com.example.web.dto.GiftDto;
import com.example.web.dto.query.GiftPagedInput;
import com.example.web.service.GiftService;
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
@RequestMapping("/Gift")
public class GiftController {

    @Autowired
    private GiftService _GiftService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<GiftDto> List(@RequestBody GiftPagedInput input) {
        return _GiftService.List(input);
    }

    @RequestMapping(value = "/FrontList", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<GiftDto> FrontList(@RequestBody GiftPagedInput input) {
        return _GiftService.FrontList(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public GiftDto Get(@RequestBody GiftPagedInput input) {
        return _GiftService.Get(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public GiftDto CreateOrEdit(@RequestBody GiftDto input) throws Exception {
        return _GiftService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        _GiftService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        _GiftService.BatchDelete(input);
    }
}
