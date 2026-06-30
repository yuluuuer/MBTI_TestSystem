package com.mbti.service;

import com.mbti.dto.response.StatsResponse;
import com.mbti.entity.ExamType;
import com.mbti.entity.TestResult;
import com.mbti.entity.User;
import com.mbti.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final TestResultRepository resultRepository;
    private final ExamTypeRepository examTypeRepository;

    private static final List<String> MBTI_TYPES = List.of(
            "INTJ", "INTP", "ENTJ", "ENTP",
            "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ",
            "ISTP", "ISFP", "ESTP", "ESFP"
    );

    public StatsResponse getStats(String examTypeId) {
        long totalUsers = userRepository.count();
        List<ExamType> examTypes = examTypeRepository.findAllByOrderByCreatedAtDesc();
        String selectedExamTypeId = normalizeSelectedExamTypeId(examTypeId, examTypes);
        long totalQuestions = selectedExamTypeId == null
                ? questionRepository.count()
                : questionRepository.countByExamTypeId(selectedExamTypeId);

        List<TestResult> allResults = resultRepository.findAll();
        List<TestResult> filteredResults = selectedExamTypeId == null
                ? allResults
                : allResults.stream()
                .filter(result -> result.getSession() != null && selectedExamTypeId.equals(result.getSession().getExamTypeId()))
                .toList();

        long totalFinished = filteredResults.size();
        long totalSessions = selectedExamTypeId == null
                ? sessionRepository.count()
                : sessionRepository.findAll().stream().filter(session -> selectedExamTypeId.equals(session.getExamTypeId())).count();

        // MBTI type distribution
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (String type : MBTI_TYPES) typeCounts.put(type, 0L);
        for (TestResult r : filteredResults) {
            typeCounts.merge(r.getMbtiType(), 1L, Long::sum);
        }

        List<StatsResponse.MbtiDistribution> mbtiDistribution = new ArrayList<>();
        for (String type : MBTI_TYPES) {
            long count = typeCounts.getOrDefault(type, 0L);
            double percentage = totalFinished > 0 ? Math.round((count * 100.0 / totalFinished) * 10) / 10.0 : 0;
            mbtiDistribution.add(StatsResponse.MbtiDistribution.builder()
                    .type(type)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        // Dimension distribution
        Map<String, Integer> dimCounts = new LinkedHashMap<>();
        for (String key : List.of("E", "I", "S", "N", "T", "F", "J", "P")) {
            dimCounts.put(key, 0);
        }
        for (TestResult r : filteredResults) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Integer> scores = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(r.getScoresJson(), Map.class);
                for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                    if (dimCounts.containsKey(entry.getKey())) {
                        dimCounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Recent activity (last 7 days)
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        Map<String, Long> dayMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = sevenDaysAgo.plusDays(i);
            dayMap.put(d.format(DateTimeFormatter.ISO_LOCAL_DATE), 0L);
        }

        for (TestResult r : filteredResults) {
            if (r.getCreatedAt() != null) {
                String dateKey = r.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                if (dayMap.containsKey(dateKey)) {
                    dayMap.merge(dateKey, 1L, Long::sum);
                }
            }
        }

        List<StatsResponse.RecentActivity> recentActivity = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dayMap.entrySet()) {
            recentActivity.add(StatsResponse.RecentActivity.builder()
                    .date(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }

        return StatsResponse.builder()
                .selectedExamTypeId(selectedExamTypeId)
                .examTypes(examTypes.stream()
                        .map(examType -> StatsResponse.ExamTypeOption.builder()
                                .id(examType.getId())
                                .name(examType.getName())
                                .build())
                        .toList())
                .overview(StatsResponse.Overview.builder()
                        .totalUsers(totalUsers)
                        .totalQuestions(totalQuestions)
                        .totalSessions(totalSessions)
                        .totalFinished(totalFinished)
                        .build())
                .mbtiDistribution(mbtiDistribution)
                .dimensionDistribution(dimCounts)
                .recentActivity(recentActivity)
                .userAnswerStatuses(buildUserAnswerStatuses(selectedExamTypeId, examTypes, filteredResults))
                .build();
    }

    private String normalizeSelectedExamTypeId(String examTypeId, List<ExamType> examTypes) {
        if (examTypeId == null || examTypeId.isBlank()) {
            return examTypes.isEmpty() ? null : examTypes.get(0).getId();
        }
        return examTypes.stream().anyMatch(examType -> examType.getId().equals(examTypeId)) ? examTypeId : null;
    }

    private List<StatsResponse.UserAnswerStatus> buildUserAnswerStatuses(String selectedExamTypeId,
                                                                          List<ExamType> examTypes,
                                                                          List<TestResult> filteredResults) {
        if (selectedExamTypeId == null) {
            return List.of();
        }

        ExamType selectedExamType = examTypes.stream()
                .filter(examType -> selectedExamTypeId.equals(examType.getId()))
                .findFirst()
                .orElse(null);
        if (selectedExamType == null) {
            return List.of();
        }

        List<User> users = selectedExamType.getParticipants() == null
                ? List.of()
                : selectedExamType.getParticipants();

        Map<String, TestResult> resultByUserId = filteredResults.stream()
                .filter(result -> result.getUserId() != null)
                .collect(Collectors.toMap(
                        TestResult::getUserId,
                        Function.identity(),
                        (left, right) -> left.getCreatedAt().isAfter(right.getCreatedAt()) ? left : right
                ));

        return users.stream()
                .map(user -> {
                    TestResult result = resultByUserId.get(user.getId());
                    boolean answered = result != null;
                    return StatsResponse.UserAnswerStatus.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .account(user.getEmail())
                            .status(answered ? "已作答" : "未作答")
                            .mbtiType(answered ? result.getMbtiType() : null)
                            .submittedAt(answered && result.getCreatedAt() != null ? result.getCreatedAt().toString() : null)
                            .build();
                })
                .toList();
    }
}
