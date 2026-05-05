package com.example.web.controller;

import com.example.web.dto.AnnouncementDto;
import com.example.web.dto.query.AnnouncementPagedInput;
import com.example.web.service.AnnouncementService;
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
@RequestMapping("/Announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService _AnnouncementService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<AnnouncementDto> List(@RequestBody AnnouncementPagedInput input) {
        return _AnnouncementService.List(input);
    }

    @RequestMapping(value = "/FrontList", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<AnnouncementDto> FrontList(@RequestBody AnnouncementPagedInput input) {
        return _AnnouncementService.FrontList(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public AnnouncementDto Get(@RequestBody AnnouncementPagedInput input) {
        return _AnnouncementService.Get(input);
    }

    @RequestMapping(value = "/FrontGet", method = RequestMethod.POST)
    @SneakyThrows
    public AnnouncementDto FrontGet(@RequestBody AnnouncementPagedInput input) {
        return _AnnouncementService.FrontGet(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public AnnouncementDto CreateOrEdit(@RequestBody AnnouncementDto input) throws Exception {
        return _AnnouncementService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        _AnnouncementService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        _AnnouncementService.BatchDelete(input);
    }
}
