package com.example.web.controller;

import com.example.web.dto.FeedbackDto;
import com.example.web.dto.query.FeedbackPagedInput;
import com.example.web.entity.Feedback;
import com.example.web.mapper.FeedbackMapper;
import com.example.web.service.FeedbackService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService _FeedbackService;

    @Autowired
    private FeedbackMapper _FeedbackMapper;

    @SneakyThrows
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    public PagedResult<FeedbackDto> List(@RequestBody FeedbackPagedInput input) {
        return _FeedbackService.List(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    public FeedbackDto Get(@RequestBody FeedbackPagedInput input) {
        return _FeedbackService.Get(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public FeedbackDto CreateOrEdit(@RequestBody FeedbackDto input) {
        return _FeedbackService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        _FeedbackService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        _FeedbackService.BatchDelete(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/Reply", method = RequestMethod.POST)
    public FeedbackDto Reply(@RequestBody FeedbackDto input) {
        return _FeedbackService.Reply(input);
    }
}
