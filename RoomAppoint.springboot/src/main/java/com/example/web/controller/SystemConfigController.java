package com.example.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.SystemConfig;
import com.example.web.enums.RoleTypeEnum;
import com.example.web.mapper.SystemConfigMapper;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.CurrentUserDto;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/SystemConfig")
public class SystemConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @PostMapping("/List")
    public List<SystemConfig> List() {
        requireAdmin();
        return systemConfigMapper.selectList(null);
    }

    @PostMapping("/GetByKey")
    public SystemConfig GetByKey(@RequestBody Map<String, String> body) {
        String key = body.get("ConfigKey");
        LambdaQueryWrapper<SystemConfig> query = Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getConfigKey, key);
        return systemConfigMapper.selectOne(query);
    }

    @PostMapping("/Save")
    public void Save(@RequestBody List<SystemConfig> configs) {
        requireAdmin();
        for (SystemConfig config : configs) {
            LambdaQueryWrapper<SystemConfig> query = Wrappers.<SystemConfig>lambdaQuery()
                    .eq(SystemConfig::getConfigKey, config.getConfigKey());
            SystemConfig existing = systemConfigMapper.selectOne(query);
            if (existing != null) {
                existing.setConfigValue(config.getConfigValue());
                existing.setDescription(config.getDescription());
                systemConfigMapper.updateById(existing);
            } else {
                systemConfigMapper.insert(config);
            }
        }
    }

    private void requireAdmin() {
        CurrentUserDto current = BaseContext.getCurrentUserDto();
        if (current == null || current.getRoleType() == null || current.getRoleType() != RoleTypeEnum.管理员) {
            throw new CustomException("无权限");
        }
    }
}
