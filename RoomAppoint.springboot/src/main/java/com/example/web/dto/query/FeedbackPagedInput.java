package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FeedbackPagedInput extends PagedInput {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("TitleLike")
    private String TitleLike;

    @JsonProperty("ContentLike")
    private String ContentLike;

    @JsonProperty("Status")
    private Integer Status;

    @JsonProperty("UserId")
    private Integer UserId;
}
