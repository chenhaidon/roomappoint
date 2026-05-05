package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.FeedbackDto;
import com.example.web.dto.query.FeedbackPagedInput;
import com.example.web.entity.Feedback;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

public interface FeedbackService extends IService<Feedback> {
    PagedResult<FeedbackDto> List(FeedbackPagedInput input);
    FeedbackDto Get(FeedbackPagedInput input);
    FeedbackDto CreateOrEdit(FeedbackDto input);
    void Delete(IdInput input);
    void BatchDelete(IdsInput input);
    FeedbackDto Reply(FeedbackDto input);
}
