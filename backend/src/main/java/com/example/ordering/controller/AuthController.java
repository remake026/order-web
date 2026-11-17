package com.example.ordering.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 测试环境的后台登录：账号密码写死在前端可预期的两个常量里。
 * 不做 token 校验，仅用于区分"进了后台"这个页面。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            return Map.of("success", true, "token", "test-token-admin");
        }
        return Map.of("success", false);
    }
}
