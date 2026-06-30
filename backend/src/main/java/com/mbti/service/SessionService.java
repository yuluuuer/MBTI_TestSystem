package com.mbti.service;

import com.mbti.dto.request.AnswerRequest;
import com.mbti.dto.request.SubmitRequest;
import com.mbti.dto.response.ApiResponse;
import com.mbti.entity.Answer;
import com.mbti.entity.ExamType;
import com.mbti.entity.TestResult;
import com.mbti.entity.TestSession;
import com.mbti.enums.SessionStatus;
import com.mbti.repository.AnswerRepository;
import com.mbti.repository.ExamTypeRepository;
import com.mbti.repository.TestResultRepository;
import com.mbti.repository.TestSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TestSessionRepository sessionRepository;
    private final AnswerRepository answerRepository;
    private final TestResultRepository resultRepository;
    private final ExamTypeRepository examTypeRepository;
    private final MbtiScoringService scoringService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> startSession(String userId, String examTypeId) {
        if (userId == null || userId.isEmpty()) {
            return Map.of("ok", false, "message", "请先登录");
        }
        ExamType examType = examTypeRepository.findById(examTypeId).orElse(null);
        String message = validateExamAccess(examType, userId);
        if (message != null) {
            return Map.of("ok", false, "message", message);
        }

        TestSession session = TestSession.builder()
                .status(SessionStatus.IN_PROGRESS)
                .build();

        if (userId != null && !userId.isEmpty()) {
            session.setUserId(userId);
        }
        if (examTypeId != null && !examTypeId.isEmpty()) {
            session.setExamTypeId(examTypeId);
        }

        session = sessionRepository.save(session);
        LocalDateTime deadline = resolveSessionDeadline(session, examType);
        return Map.of(
                "ok", true,
                "sessionId", session.getId(),
                "startedAt", session.getStartedAt().toString(),
                "deadlineAt", deadline != null ? deadline.toString() : ""
        );
    }

    @Transactional
    public ApiResponse saveAnswer(AnswerRequest request) {
        Optional<Answer> existing = answerRepository.findBySessionIdAndQuestionId(
                request.getSessionId(), request.getQuestionId());

        if (existing.isPresent()) {
            Answer answer = existing.get();
            answer.setOptionId(request.getOptionId());
            answerRepository.save(answer);
        } else {
            Answer answer = Answer.builder()
                    .sessionId(request.getSessionId())
                    .questionId(request.getQuestionId())
                    .optionId(request.getOptionId())
                    .build();
            answerRepository.save(answer);
        }

        return ApiResponse.success();
    }

    @Transactional
    public Map<String, Object> submitSession(SubmitRequest request) {
        TestSession session = sessionRepository.findById(request.getSessionId())
                .orElse(null);

        if (session == null) {
            return Map.of("ok", false, "message", "测试会话不存在");
        }

        if (isSessionExpired(session)) {
            return Map.of("ok", false, "message", "测试已超时");
        }

        var answers = answerRepository.findBySessionId(session.getId());
        if (answers.isEmpty()) {
            return Map.of("ok", false, "message", "请先完成答题");
        }

        // Calculate scores
        Map<String, Integer> scores = scoringService.calculateScores(answers);
        String mbtiType = scoringService.buildMbtiType(scores);

        // Create or update result
        Optional<TestResult> existingResult = resultRepository.findBySessionId(session.getId());
        TestResult result;
        if (existingResult.isPresent()) {
            result = existingResult.get();
            result.setMbtiType(mbtiType);
            try {
                result.setScoresJson(objectMapper.writeValueAsString(scores));
            } catch (Exception e) {
                result.setScoresJson("{}");
            }
        } else {
            String scoresJson;
            try {
                scoresJson = objectMapper.writeValueAsString(scores);
            } catch (Exception e) {
                scoresJson = "{}";
            }
            result = TestResult.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .mbtiType(mbtiType)
                    .scoresJson(scoresJson)
                    .build();
        }
        result = resultRepository.save(result);

        // Update session status
        session.setStatus(SessionStatus.FINISHED);
        session.setFinishedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return Map.of(
                "ok", true,
                "resultId", result.getId(),
                "mbtiType", mbtiType
        );
    }

    private String validateExamAccess(ExamType examType, String userId) {
        if (examType == null || !examType.isActive()) {
            return "测试不存在或已禁用";
        }
        LocalDateTime now = LocalDateTime.now();
        if (examType.getStartTime() != null && now.isBefore(examType.getStartTime())) {
            return "测试尚未开始";
        }
        if (examType.getEndTime() != null && now.isAfter(examType.getEndTime())) {
            return "测试已截止";
        }
        boolean assigned = examType.getParticipants() != null
                && examType.getParticipants().stream().anyMatch(user -> userId.equals(user.getId()));
        return assigned ? null : "你未被分配参加该测试";
    }

    private boolean isSessionExpired(TestSession session) {
        if (session.getStartedAt() == null || session.getExamTypeId() == null) {
            return false;
        }
        ExamType examType = examTypeRepository.findById(session.getExamTypeId()).orElse(null);
        Integer duration = examType != null ? examType.getDurationMinutes() : null;
        if (duration == null || duration < 1) {
            return false;
        }
        LocalDateTime deadline = resolveSessionDeadline(session, examType);
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    private LocalDateTime resolveSessionDeadline(TestSession session, ExamType examType) {
        if (session.getStartedAt() == null || examType == null) {
            return null;
        }

        Integer duration = examType.getDurationMinutes();
        LocalDateTime deadline = duration != null && duration > 0
                ? session.getStartedAt().plusMinutes(duration)
                : null;

        if (examType.getEndTime() != null && (deadline == null || examType.getEndTime().isBefore(deadline))) {
            deadline = examType.getEndTime();
        }

        return deadline;
    }
}
