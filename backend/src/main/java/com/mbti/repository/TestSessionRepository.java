package com.mbti.repository;

import com.mbti.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, String> {
    List<TestSession> findByUserId(String userId);
}
