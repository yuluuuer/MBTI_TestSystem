package com.mbti.service;

import com.mbti.dto.auth.AuthResponse;
import com.mbti.dto.auth.LoginRequest;
import com.mbti.dto.auth.RegisterRequest;
import com.mbti.entity.ExamType;
import com.mbti.entity.User;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.UserRepository;
import com.mbti.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ExamTypeRepository examTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            return AuthResponse.builder()
                    .ok(false)
                    .build();
        }

        User user = User.builder()
                .email(email)
                .name(normalizeText(request.getName()))
                .phone(normalizeText(request.getPhone()))
                .gender(normalizeText(request.getGender()))
                .birthDate(request.getBirthDate())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        addUserToAllExamTypes(user);
        String token = tokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .ok(true)
                .token(token)
                .user(toUserInfo(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || Boolean.FALSE.equals(user.getIsActive()) || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return AuthResponse.builder().ok(false).build();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .ok(true)
                .token(token)
                .user(toUserInfo(user))
                .build();
    }

    public AuthResponse.UserInfo getCurrentUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (Boolean.FALSE.equals(user.getIsActive())) return null;
        return toUserInfo(user);
    }

    private AuthResponse.UserInfo toUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .isActive(user.getIsActive())
                .role(user.getRole().name())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
}
