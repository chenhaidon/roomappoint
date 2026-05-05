package com.example.web.controller;

import com.example.web.enums.RoleTypeEnum;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.CurrentUserDto;
import com.example.web.tools.exception.CustomException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/Ops")
public class OpsController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OpsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/Status")
    public OpsStatusDto status() {
        requireAdmin();

        OpsStatusDto dto = new OpsStatusDto();
        dto.setNow(Instant.now().toString());
        dto.setUptimeSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000);

        dto.setBackend(checkBackend());
        dto.setMysql(checkMysql());
        dto.setNginx(checkNginx());

        return dto;
    }

    private void requireAdmin() {
        CurrentUserDto current = BaseContext.getCurrentUserDto();
        if (current == null || current.getRoleType() == null || current.getRoleType() != RoleTypeEnum.管理员) {
            throw new CustomException("无权限");
        }
    }

    private ServiceStatus checkBackend() {
        ServiceStatus s = new ServiceStatus();
        s.setOk(true);
        return s;
    }

    private ServiceStatus checkMysql() {
        ServiceStatus s = new ServiceStatus();
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            s.setOk(one != null && one == 1);
        } catch (Exception e) {
            s.setOk(false);
            s.setMessage(e.getMessage());
        }
        return s;
    }

    private HttpServiceStatus checkNginx() {
        HttpServiceStatus s = new HttpServiceStatus();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://frontend-nginx/"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
            s.setHttpStatus(resp.statusCode());
            s.setOk(resp.statusCode() >= 200 && resp.statusCode() < 400);
        } catch (Exception e) {
            s.setOk(false);
            s.setMessage(e.getMessage());
        }
        return s;
    }

    @Data
    public static class OpsStatusDto {
        private String now;
        private long uptimeSeconds;
        private ServiceStatus backend;
        private ServiceStatus mysql;
        private HttpServiceStatus nginx;
    }

    @Data
    public static class ServiceStatus {
        private boolean ok;
        private String message;
    }

    @Data
    public static class HttpServiceStatus extends ServiceStatus {
        private Integer httpStatus;
    }
}
