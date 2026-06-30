package com.mbti.repository;

import com.mbti.entity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamTypeRepository extends JpaRepository<ExamType, String> {
    List<ExamType> findByIsActiveTrueOrderByCreatedAtAsc();
    List<ExamType> findAllByOrderByCreatedAtDesc();

    @Query("""
            select distinct et from ExamType et
            join et.participants p
            where et.isActive = true
              and p.id = :userId
            order by et.createdAt asc
            """)
    List<ExamType> findVisibleActiveForUser(String userId);
}
