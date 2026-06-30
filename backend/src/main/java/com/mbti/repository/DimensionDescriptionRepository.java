package com.mbti.repository;

import com.mbti.entity.DimensionDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimensionDescriptionRepository extends JpaRepository<DimensionDescription, String> {
    List<DimensionDescription> findByExamTypeIdOrderByCodeAsc(String examTypeId);
    Optional<DimensionDescription> findByExamTypeIdAndCode(String examTypeId, String code);
}
