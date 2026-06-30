package com.mbti.controller;

import com.mbti.dto.request.ChangePasswordRequest;
import com.mbti.dto.request.UpdateProfileRequest;
import com.mbti.dto.response.ApiResponse;
import com.mbti.entity.User;
import com.mbti.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }

        String userId = authentication.getName();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }

        user.setName(request.getName());
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }

        User user = userRepository.findById(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(400).body(Map.of("message", "原密码错误"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }
}
