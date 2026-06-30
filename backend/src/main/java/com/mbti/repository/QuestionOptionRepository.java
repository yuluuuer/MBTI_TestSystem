package com.mbti.repository;

import com.mbti.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, String> {
    List<QuestionOption> findByQuestionId(String questionId);
    List<QuestionOption> findByIdIn(List<String> ids);
}
