package com.mbti.repository;

import com.mbti.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, String> {
    List<TestResult> findByUserIdOrderByCreatedAtDesc(String userId);
    List<TestResult> findByUserIdAndSessionExamTypeIdOrderByCreatedAtDesc(String userId, String examTypeId);
    Optional<TestResult> findBySessionId(String sessionId);
    void deleteByUserId(String userId);
    boolean existsByUserIdAndSessionExamTypeId(String userId, String examTypeId);
}
