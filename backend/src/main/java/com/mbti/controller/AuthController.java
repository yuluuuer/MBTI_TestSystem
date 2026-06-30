package com.mbti.controller;

import com.mbti.dto.auth.AuthResponse;
import com.mbti.dto.auth.LoginRequest;
import com.mbti.dto.auth.RegisterRequest;
import com.mbti.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final String COOKIE_NAME = "mbti_auth_token";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletResponse response) {
        AuthResponse result = authService.register(request);
        if (!result.isOk()) {
            return ResponseEntity.status(409).body(Map.of("message", "账号已注册"));
        }

        addAuthCookie(response, result.getToken());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "user", result.getUser()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        AuthResponse result = authService.login(request);
        if (!result.isOk()) {
            return ResponseEntity.status(401).body(Map.of("message", "账号或密码错误"));
        }

        addAuthCookie(response, result.getToken());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "user", result.getUser()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("user", null);
            return ResponseEntity.ok(body);
        }
        AuthResponse.UserInfo user = authService.getCurrentUser(authentication.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("user", user);
        return ResponseEntity.ok(body);
    }

    private void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(COOKIE_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
