package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BalanceRecordPagedInput extends PagedInput {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("SourceLike")
    private String SourceLike;

    @JsonProperty("TitleLike")
    private String TitleLike;
}
