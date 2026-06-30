package com.mbti.controller;

import com.mbti.entity.ExamType;
import com.mbti.entity.TestResult;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final TestResultRepository resultRepository;
    private final ExamTypeRepository examTypeRepository;

    @GetMapping
    public ResponseEntity<?> history(Authentication authentication,
                                     @RequestParam(required = false) String examTypeId) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }

        String userId = authentication.getName();
        List<TestResult> results;

        if (examTypeId != null && !examTypeId.isEmpty()) {
            results = resultRepository.findByUserIdAndSessionExamTypeIdOrderByCreatedAtDesc(userId, examTypeId);
        } else {
            results = resultRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TestResult r : results) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("mbtiType", r.getMbtiType());
            item.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);

            // Get exam type info from session
            if (r.getSession() != null && r.getSession().getExamTypeId() != null) {
                examTypeRepository.findById(r.getSession().getExamTypeId()).ifPresent(et -> {
                    item.put("session", Map.of(
                            "examType", Map.of(
                                    "id", et.getId(),
                                    "name", et.getName()
                            )
                    ));
                });
            }
            resultList.add(item);
        }

        return ResponseEntity.ok(Map.of("results", resultList));
    }
}
