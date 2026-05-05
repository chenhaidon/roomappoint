package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.AnnouncementDto;
import com.example.web.dto.query.AnnouncementPagedInput;
import com.example.web.entity.Announcement;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

public interface AnnouncementService extends IService<Announcement> {

    PagedResult<AnnouncementDto> List(AnnouncementPagedInput input);

    PagedResult<AnnouncementDto> FrontList(AnnouncementPagedInput input);

    AnnouncementDto CreateOrEdit(AnnouncementDto input);

    AnnouncementDto Get(AnnouncementPagedInput input);

    AnnouncementDto FrontGet(AnnouncementPagedInput input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);
}
