package com.mbti.controller.admin;

import com.mbti.dto.request.UserAdminRequest;
import com.mbti.entity.ExamType;
import com.mbti.entity.TestSession;
import com.mbti.entity.User;
import com.mbti.enums.UserRole;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.TestResultRepository;
import com.mbti.repository.TestSessionRepository;
import com.mbti.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final ExamTypeRepository examTypeRepository;
    private final TestSessionRepository testSessionRepository;
    private final TestResultRepository testResultRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> list() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = users.stream()
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("email", u.getEmail());
                    map.put("name", u.getName());
                    map.put("phone", u.getPhone());
                    map.put("gender", u.getGender());
                    map.put("birthDate", u.getBirthDate() != null ? u.getBirthDate().toString() : null);
                    map.put("isActive", u.getIsActive());
                    map.put("role", u.getRole().name());
                    map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    return map;
                })
                .toList();
        return ResponseEntity.ok(Map.of("users", result));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody UserAdminRequest request) {
        String email = normalizeText(request.getEmail());
        if (email == null) {
            return ResponseEntity.status(400).body(Map.of("message", "账号不能为空"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of("message", "账号已存在"));
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.status(400).body(Map.of("message", "密码至少 6 位"));
        }

        UserRole role = "ADMIN".equals(request.getRole()) ? UserRole.ADMIN : UserRole.USER;
        User user = User.builder()
                .email(email)
                .name(normalizeText(request.getName()))
                .phone(normalizeText(request.getPhone()))
                .gender(normalizeText(request.getGender()))
                .birthDate(request.getBirthDate())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .role(role)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        if (role == UserRole.USER) {
            addUserToAllExamTypes(user);
        }
        return ResponseEntity.ok(Map.of("ok", true, "message", "用户已新增"));
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserAdminRequest request,
                                        Authentication authentication) {
        String adminId = authentication.getName();
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }

        String action = request.getAction();
        if ("resetPassword".equals(action)) {
            user.setPasswordHash(passwordEncoder.encode("123456"));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("ok", true, "message", "密码已重置为 123456"));
        } else if ("changePassword".equals(action)) {
            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                return ResponseEntity.status(400).body(Map.of("message", "请输入新密码"));
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("ok", true, "message", "密码修改成功"));
        } else if ("updateProfile".equals(action)) {
            user.setName(normalizeText(request.getName()));
            user.setPhone(normalizeText(request.getPhone()));
            user.setGender(normalizeText(request.getGender()));
            user.setBirthDate(request.getBirthDate());
            user.setIsActive(request.getIsActive() == null || request.getIsActive());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("ok", true, "message", "用户信息已更新"));
        }

        return ResponseEntity.status(400).body(Map.of("message", "参数错误"));
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> body,
                                        Authentication authentication) {
        String userId = body.get("userId");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "参数错误"));
        }

        String adminId = authentication.getName();
        if (userId.equals(adminId)) {
            return ResponseEntity.status(400).body(Map.of("message", "不能删除自己"));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }

        List<ExamType> examTypes = examTypeRepository.findAll();
        examTypes.forEach(examType -> {
            boolean removed = examType.getParticipants().removeIf(participant -> userId.equals(participant.getId()));
            if (removed) {
                examTypeRepository.save(examType);
            }
        });

        testResultRepository.deleteByUserId(userId);
        List<TestSession> sessions = testSessionRepository.findByUserId(userId);
        testSessionRepository.deleteAll(sessions);
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    private void addUserToAllExamTypes(User user) {
        List<ExamType> examTypes = examTypeRepository.findAll();
        for (ExamType examType : examTypes) {
            boolean exists = examType.getParticipants().stream()
                    .anyMatch(participant -> user.getId().equals(participant.getId()));
            if (!exists) {
                examType.getParticipants().add(user);
                examTypeRepository.save(examType);
            }
        }
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
