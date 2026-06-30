package com.mbti.repository;

import com.mbti.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {
    Optional<Answer> findBySessionIdAndQuestionId(String sessionId, String questionId);
    List<Answer> findBySessionId(String sessionId);
}
