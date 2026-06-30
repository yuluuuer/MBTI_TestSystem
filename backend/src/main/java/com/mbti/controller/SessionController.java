package com.mbti.controller;

import com.mbti.dto.request.AnswerRequest;
import com.mbti.dto.request.SessionStartRequest;
import com.mbti.dto.request.SubmitRequest;
import com.mbti.dto.response.ApiResponse;
import com.mbti.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody(required = false) SessionStartRequest request,
                                   Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        String examTypeId = request != null ? request.getExamTypeId() : null;
        Map<String, Object> result = sessionService.startSession(userId, examTypeId);
        if (Boolean.FALSE.equals(result.get("ok"))) {
            return ResponseEntity.status(400).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/answer")
    public ResponseEntity<?> answer(@Valid @RequestBody AnswerRequest request) {
        ApiResponse result = sessionService.saveAnswer(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@Valid @RequestBody SubmitRequest request) {
        Map<String, Object> result = sessionService.submitSession(request);
        if (Boolean.FALSE.equals(result.get("ok"))) {
            String message = (String) result.get("message");
            if ("测试会话不存在".equals(message)) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.status(400).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
