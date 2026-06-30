package com.mbti.repository;

import com.mbti.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findByIsActiveTrueAndExamTypeIdOrderBySortOrderAsc(String examTypeId);
    List<Question> findByIsActiveTrueOrderBySortOrderAsc();
    List<Question> findByExamTypeIdOrderBySortOrderAsc(String examTypeId);
    List<Question> findAllByOrderBySortOrderAsc();
    long countByExamTypeId(String examTypeId);
    long countByIsActiveTrueAndExamTypeId(String examTypeId);

    @Query("""
            select distinct q from Question q
            join q.options opt
            where (:examTypeId is null or q.examTypeId = :examTypeId)
              and (:dimension is null or opt.dimension = :dimension)
            order by q.sortOrder asc
            """)
    List<Question> findByFilters(String examTypeId, String dimension);
}
