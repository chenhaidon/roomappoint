package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName("`systemconfig`")
public class SystemConfig extends BaseEntity {

    @JsonProperty("ConfigKey")
    @TableField(value = "ConfigKey")
    private String ConfigKey;

    @JsonProperty("ConfigValue")
    @TableField(value = "ConfigValue", updateStrategy = FieldStrategy.IGNORED)
    private String ConfigValue;

    @JsonProperty("Description")
    @TableField(value = "Description", updateStrategy = FieldStrategy.IGNORED)
    private String Description;
}
