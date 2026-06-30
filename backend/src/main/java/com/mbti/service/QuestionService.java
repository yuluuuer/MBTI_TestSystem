package com.mbti.service;

import com.mbti.dto.response.QuestionResponse;
import com.mbti.dto.response.QuestionsMetaResponse;
import com.mbti.entity.ExamType;
import com.mbti.entity.Question;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamTypeRepository examTypeRepository;

    private static final int DEFAULT_TOTAL = 36;
    private static final List<String> PAIRS = List.of("EI", "SN", "TF", "JP");

    public QuestionsMetaResponse getRandomQuestions(String examTypeId, String userId) {
        List<Question> allQuestions;
        int targetTotal = DEFAULT_TOTAL;
        if (examTypeId != null && !examTypeId.isEmpty()) {
            ExamType examType = examTypeRepository.findById(examTypeId).orElse(null);
            if (!canAccessExam(examType, userId)) {
                allQuestions = List.of();
            } else {
                targetTotal = normalizeTargetTotal(examType.getQuestionLimit());
                allQuestions = questionRepository.findByIsActiveTrueAndExamTypeIdOrderBySortOrderAsc(examTypeId);
            }
        } else {
            allQuestions = questionRepository.findByIsActiveTrueOrderBySortOrderAsc();
        }

        // Group questions by pair
        Map<String, List<Question>> groups = new LinkedHashMap<>();
        for (String pair : PAIRS) {
            groups.put(pair, new ArrayList<>());
        }

        for (Question q : allQuestions) {
            String pair = inferPair(q);
            if (pair != null) {
                groups.get(pair).add(q);
            }
        }

        // Shuffle each group
        for (List<Question> group : groups.values()) {
            Collections.shuffle(group);
        }

        // Select questions evenly from each pair
        int perPairTarget = Math.max(1, targetTotal / PAIRS.size());
        List<Question> selected = new ArrayList<>();

        for (String pair : PAIRS) {
            List<Question> group = groups.get(pair);
            int take = Math.min(perPairTarget, group.size());
            selected.addAll(group.subList(0, take));
            group.subList(0, take).clear();
        }

        // Fill remaining slots
        int remain = targetTotal - selected.size();
        while (remain > 0) {
            boolean pushed = false;
            for (String pair : PAIRS) {
                List<Question> group = groups.get(pair);
                if (!group.isEmpty()) {
                    selected.add(group.remove(0));
                    remain--;
                    pushed = true;
                    if (remain == 0) break;
                }
            }
            if (!pushed) break;
        }

        // Shuffle final selection
        Collections.shuffle(selected);

        // Count per pair
        Map<String, Integer> pairCounts = new LinkedHashMap<>();
        for (String pair : PAIRS) pairCounts.put(pair, 0);
        for (Question q : selected) {
            String pair = inferPair(q);
            if (pair != null) {
                pairCounts.merge(pair, 1, Integer::sum);
            }
        }

        // Map to response
        List<QuestionResponse> questionResponses = selected.stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .sortOrder(q.getSortOrder())
                        .options(q.getOptions().stream()
                                .map(op -> QuestionResponse.OptionResponse.builder()
                                        .id(op.getId())
                                        .label(op.getLabel())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return QuestionsMetaResponse.builder()
                .questions(questionResponses)
                .meta(QuestionsMetaResponse.Meta.builder()
                        .total(questionResponses.size())
                        .target(targetTotal)
                        .pairCounts(pairCounts)
                        .build())
                .build();
    }

    private int normalizeTargetTotal(Integer questionLimit) {
        if (questionLimit == null || questionLimit < 1) {
            return DEFAULT_TOTAL;
        }
        return Math.min(questionLimit, 200);
    }

    private boolean canAccessExam(ExamType examType, String userId) {
        if (examType == null || !examType.isActive() || userId == null) return false;
        LocalDateTime now = LocalDateTime.now();
        if (examType.getStartTime() != null && now.isBefore(examType.getStartTime())) return false;
        if (examType.getEndTime() != null && now.isAfter(examType.getEndTime())) return false;
        return examType.getParticipants() != null
                && examType.getParticipants().stream().anyMatch(user -> userId.equals(user.getId()));
    }

    private String inferPair(Question question) {
        Set<String> dimensions = new HashSet<>();
        for (var option : question.getOptions()) {
            dimensions.add(option.getDimension());
        }
        if (dimensions.contains("E") && dimensions.contains("I")) return "EI";
        if (dimensions.contains("S") && dimensions.contains("N")) return "SN";
        if (dimensions.contains("T") && dimensions.contains("F")) return "TF";
        if (dimensions.contains("J") && dimensions.contains("P")) return "JP";
        return null;
    }
}
