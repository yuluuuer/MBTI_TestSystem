package com.mbti.controller;

import com.mbti.entity.ExamType;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-types")
@RequiredArgsConstructor
public class ExamTypeController {

    private final ExamTypeRepository examTypeRepository;
    private final com.mbti.repository.QuestionRepository questionRepository;
    private final TestResultRepository testResultRepository;

    @GetMapping
    public ResponseEntity<?> listActive(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        List<ExamType> examTypes = userId == null
                ? List.of()
                : examTypeRepository.findVisibleActiveForUser(userId);
        List<Map<String, Object>> result = examTypes.stream()
                .map(et -> toMap(et, userId))
                .toList();
        return ResponseEntity.ok(Map.of("examTypes", result));
    }

    private Map<String, Object> toMap(ExamType et, String userId) {
        LocalDateTime now = LocalDateTime.now();
        boolean notStarted = et.getStartTime() != null && now.isBefore(et.getStartTime());
        boolean ended = et.getEndTime() != null && now.isAfter(et.getEndTime());
        boolean hasSubmitted = userId != null && testResultRepository.existsByUserIdAndSessionExamTypeId(userId, et.getId());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", et.getId());
        map.put("name", et.getName());
        map.put("description", et.getDescription());
        map.put("createdAt", et.getCreatedAt() != null ? et.getCreatedAt().toString() : null);
        map.put("startTime", et.getStartTime() != null ? et.getStartTime().toString() : null);
        map.put("endTime", et.getEndTime() != null ? et.getEndTime().toString() : null);
        map.put("durationMinutes", et.getDurationMinutes());
        long availableQuestionCount = questionRepository.countByIsActiveTrueAndExamTypeId(et.getId());
        int questionLimit = normalizeQuestionLimit(et.getQuestionLimit());
        map.put("questionCount", Math.min(questionLimit, (int) availableQuestionCount));
        map.put("availableQuestionCount", availableQuestionCount);
        map.put("questionLimit", questionLimit);
        map.put("canStart", !notStarted && !ended && !hasSubmitted);
        map.put("hasSubmitted", hasSubmitted);
        map.put("statusText", resolveStatusText(notStarted, ended, hasSubmitted));
        return map;
    }

    private String resolveStatusText(boolean notStarted, boolean ended, boolean hasSubmitted) {
        if (hasSubmitted) return "已作答";
        if (ended) return "未作答";
        if (notStarted) return "未开始";
        return "可作答";
    }

    private int normalizeQuestionLimit(Integer questionLimit) {
        if (questionLimit == null || questionLimit < 1) {
            return 36;
        }
        return Math.min(questionLimit, 200);
    }
}
