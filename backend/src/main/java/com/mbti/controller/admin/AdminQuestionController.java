package com.mbti.controller.admin;

import com.mbti.dto.request.QuestionCreateRequest;
import com.mbti.dto.request.QuestionUpdateRequest;
import com.mbti.dto.response.ApiResponse;
import com.mbti.entity.Question;
import com.mbti.entity.QuestionOption;
import com.mbti.repository.QuestionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionRepository questionRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String examTypeId,
                                  @RequestParam(required = false) String dimension) {
        String normalizedExamTypeId = normalizeParam(examTypeId);
        String normalizedDimension = normalizeParam(dimension);
        List<Question> questions = normalizedExamTypeId != null || normalizedDimension != null
                ? questionRepository.findByFilters(normalizedExamTypeId, normalizedDimension)
                : questionRepository.findAllByOrderBySortOrderAsc();
        return ResponseEntity.ok(Map.of("questions", questions.stream()
                .map(this::toResponse)
                .toList()));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody QuestionCreateRequest request) {
        Question question = Question.builder()
                .title(request.getTitle())
                .sortOrder(request.getSortOrder())
                .isActive(request.isActive())
                .examTypeId(request.getExamTypeId())
                .build();

        Question saved = questionRepository.save(question);
        saved.getOptions().addAll(buildOptions(request.getOptions(), saved.getId()));
        saved = questionRepository.save(saved);
        return ResponseEntity.ok(Map.of("ok", true, "question", toResponse(saved)));
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody QuestionUpdateRequest request) {
        Question question = questionRepository.findById(request.getId()).orElse(null);
        if (question == null) {
            return ResponseEntity.status(404).body(Map.of("message", "题目不存在"));
        }

        question.setTitle(request.getTitle());
        question.setSortOrder(request.getSortOrder());
        question.setActive(request.isActive());

        // Replace all options
        question.getOptions().clear();
        question.getOptions().addAll(buildOptions(request.getOptions(), question.getId()));

        Question saved = questionRepository.save(question);
        return ResponseEntity.ok(Map.of("ok", true, "question", toResponse(saved)));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "参数错误"));
        }
        questionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private Map<String, Object> toResponse(Question question) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", question.getId());
        body.put("title", question.getTitle());
        body.put("sortOrder", question.getSortOrder());
        body.put("isActive", question.isActive());
        body.put("examTypeId", question.getExamTypeId());
        body.put("options", question.getOptions().stream()
                .map(this::toOptionResponse)
                .toList());
        return body;
    }

    private List<QuestionOption> buildOptions(List<QuestionCreateRequest.OptionRequest> requestOptions, String questionId) {
        List<QuestionOption> options = new ArrayList<>();
        for (QuestionCreateRequest.OptionRequest opt : requestOptions) {
            options.add(QuestionOption.builder()
                    .label(opt.getLabel())
                    .dimension(opt.getDimension())
                    .weight(opt.getWeight())
                    .questionId(questionId)
                    .build());
        }
        return options;
    }

    private Map<String, Object> toOptionResponse(QuestionOption option) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", option.getId());
        body.put("label", option.getLabel());
        body.put("dimension", option.getDimension());
        body.put("weight", option.getWeight());
        return body;
    }

    private String normalizeParam(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
