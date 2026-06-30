package com.mbti.controller;

import com.mbti.dto.response.QuestionsMetaResponse;
import com.mbti.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<?> getQuestions(@RequestParam(required = false) String examTypeId,
                                          Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        QuestionsMetaResponse response = questionService.getRandomQuestions(examTypeId, userId);
        return ResponseEntity.ok(response);
    }
}
