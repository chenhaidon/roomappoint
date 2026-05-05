package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GiftPagedInput extends PagedInput {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("NameLike")
    private String NameLike;

    @JsonProperty("Status")
    private String Status;
}
