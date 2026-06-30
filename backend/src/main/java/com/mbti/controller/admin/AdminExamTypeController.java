package com.mbti.controller.admin;

import com.mbti.dto.request.ExamTypeCreateRequest;
import com.mbti.dto.request.ExamTypeUpdateRequest;
import com.mbti.entity.ExamType;
import com.mbti.entity.User;
import com.mbti.enums.UserRole;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.QuestionRepository;
import com.mbti.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/exam-types")
@RequiredArgsConstructor
public class AdminExamTypeController {

    private final ExamTypeRepository examTypeRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> list() {
        List<ExamType> examTypes = examTypeRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = examTypes.stream()
                .map(et -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", et.getId());
                    map.put("name", et.getName());
                    map.put("description", et.getDescription());
                    map.put("isActive", et.isActive());
                    map.put("createdAt", et.getCreatedAt() != null ? et.getCreatedAt().toString() : null);
                    map.put("startTime", et.getStartTime() != null ? et.getStartTime().toString() : null);
                    map.put("endTime", et.getEndTime() != null ? et.getEndTime().toString() : null);
                    map.put("durationMinutes", et.getDurationMinutes());
                    map.put("questionLimit", normalizeQuestionLimit(et.getQuestionLimit()));
                    map.put("participantIds", et.getParticipants().stream().map(User::getId).toList());
                    map.put("participants", et.getParticipants().stream()
                            .map(user -> Map.of(
                                    "id", user.getId(),
                                    "name", user.getName() != null ? user.getName() : "",
                                    "email", user.getEmail()
                            ))
                            .toList());
                    map.put("_count", Map.of("questions", questionRepository.countByExamTypeId(et.getId())));
                    return map;
                })
                .toList();
        return ResponseEntity.ok(Map.of("examTypes", result));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ExamTypeCreateRequest request) {
        ExamType examType = ExamType.builder()
                .name(request.getName())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(normalizeDuration(request.getDurationMinutes()))
                .questionLimit(normalizeQuestionLimit(request.getQuestionLimit()))
                .isActive(true)
                .build();
        examType.setParticipants(resolveParticipants(request.getParticipantIds()));
        ExamType saved = examTypeRepository.save(examType);
        return ResponseEntity.ok(Map.of("ok", true, "examType", saved));
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody ExamTypeUpdateRequest request) {
        ExamType examType = examTypeRepository.findById(request.getId()).orElse(null);
        if (examType == null) {
            return ResponseEntity.status(404).body(Map.of("message", "考核类型不存在"));
        }

        if (request.getName() != null) examType.setName(request.getName());
        if (request.getDescription() != null) examType.setDescription(request.getDescription());
        if (request.getIsActive() != null) examType.setActive(request.getIsActive());
        examType.setStartTime(request.getStartTime());
        examType.setEndTime(request.getEndTime());
        examType.setDurationMinutes(normalizeDuration(request.getDurationMinutes()));
        examType.setQuestionLimit(normalizeQuestionLimit(request.getQuestionLimit()));
        if (request.getParticipantIds() != null) {
            examType.setParticipants(resolveParticipants(request.getParticipantIds()));
        }

        ExamType saved = examTypeRepository.save(examType);
        return ResponseEntity.ok(Map.of("ok", true, "examType", saved));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "参数错误"));
        }
        examTypeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/{id}/participants/import")
    public ResponseEntity<?> importParticipants(@PathVariable String id,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        ExamType examType = examTypeRepository.findById(id).orElse(null);
        if (examType == null) {
            return ResponseEntity.status(404).body(Map.of("message", "测试不存在"));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "请选择导入文件"));
        }

        ImportResult result = importUsersFromContent(new String(file.getBytes(), StandardCharsets.UTF_8));
        if (result.importedUsers().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of(
                    "message", "没有可导入的参测人员",
                    "skippedRows", result.skippedRows()
            ));
        }

        LinkedHashMap<String, User> merged = new LinkedHashMap<>();
        for (User user : examType.getParticipants()) {
            merged.put(user.getId(), user);
        }
        for (User user : result.importedUsers()) {
            merged.put(user.getId(), user);
        }
        examType.setParticipants(new ArrayList<>(merged.values()));
        examTypeRepository.save(examType);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "importedCount", result.importedUsers().size(),
                "createdCount", result.createdCount(),
                "updatedCount", result.updatedCount(),
                "skippedRows", result.skippedRows(),
                "participantCount", examType.getParticipants().size()
        ));
    }

    private List<User> resolveParticipants(List<String> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = participantIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return userRepository.findAllById(ids);
    }

    private Integer normalizeDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes < 1) {
            return 30;
        }
        return Math.min(durationMinutes, 600);
    }

    private Integer normalizeQuestionLimit(Integer questionLimit) {
        if (questionLimit == null || questionLimit < 1) {
            return 36;
        }
        return Math.min(questionLimit, 200);
    }

    private ImportResult importUsersFromContent(String content) {
        List<User> importedUsers = new ArrayList<>();
        List<String> skippedRows = new ArrayList<>();
        int createdCount = 0;
        int updatedCount = 0;
        String[] lines = content.replace("\uFEFF", "").split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] columns = line.split("[,，]");
            if (columns.length < 4) {
                skippedRows.add("第 " + (i + 1) + " 行格式错误");
                continue;
            }

            String phone = normalizeText(columns[0]);
            String name = normalizeText(columns[1]);
            String gender = normalizeGender(columns[2]);
            LocalDate birthDate = parseBirthDate(columns[3]);
            if ("手机号".equals(phone) || "手机号码".equals(phone)) {
                continue;
            }
            if (phone == null || name == null || birthDate == null) {
                skippedRows.add("第 " + (i + 1) + " 行缺少手机号、姓名或出生日期");
                continue;
            }

            User user = userRepository.findByEmail(phone).orElse(null);
            if (user == null) {
                user = User.builder()
                        .email(phone)
                        .name(name)
                        .phone(phone)
                        .gender(gender)
                        .birthDate(birthDate)
                        .isActive(true)
                        .role(UserRole.USER)
                        .passwordHash(passwordEncoder.encode("123456"))
                        .build();
                createdCount++;
            } else {
                user.setName(name);
                user.setPhone(phone);
                user.setGender(gender);
                user.setBirthDate(birthDate);
                user.setIsActive(true);
                if (user.getRole() == null) {
                    user.setRole(UserRole.USER);
                }
                updatedCount++;
            }
            importedUsers.add(userRepository.save(user));
        }

        return new ImportResult(importedUsers, createdCount, updatedCount, skippedRows);
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeGender(String value) {
        String gender = normalizeText(value);
        if (gender == null) return null;
        if ("M".equalsIgnoreCase(gender) || "男".equals(gender) || "MALE".equalsIgnoreCase(gender)) {
            return "男";
        }
        if ("F".equalsIgnoreCase(gender) || "女".equals(gender) || "FEMALE".equalsIgnoreCase(gender)) {
            return "女";
        }
        return gender;
    }

    private LocalDate parseBirthDate(String value) {
        String birthDate = normalizeText(value);
        if (birthDate == null) return null;
        String normalized = birthDate
                .replace('/', '-')
                .replace('.', '-')
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "");
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("uuuu-M-d"),
                DateTimeFormatter.ofPattern("uuuu-MM-dd")
        )) {
            try {
                return LocalDate.parse(normalized, formatter);
            } catch (Exception ignored) {
                // Try next supported format.
            }
        }
        return null;
    }

    private record ImportResult(List<User> importedUsers,
                                int createdCount,
                                int updatedCount,
                                List<String> skippedRows) {
    }
}
