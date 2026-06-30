package com.mbti.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dimension_description",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_type_id", "code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String examTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examTypeId", insertable = false, updatable = false)
    @JsonIgnore
    private ExamType examType;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
