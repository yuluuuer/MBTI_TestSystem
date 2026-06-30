package com.mbti.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private Overview overview;
    private String selectedExamTypeId;
    private List<ExamTypeOption> examTypes;
    private List<MbtiDistribution> mbtiDistribution;
    private Map<String, Integer> dimensionDistribution;
    private List<RecentActivity> recentActivity;
    private List<UserAnswerStatus> userAnswerStatuses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private long totalUsers;
        private long totalQuestions;
        private long totalSessions;
        private long totalFinished;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MbtiDistribution {
        private String type;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String date;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamTypeOption {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnswerStatus {
        private String userId;
        private String name;
        private String account;
        private String status;
        private String mbtiType;
        private String submittedAt;
    }
}
