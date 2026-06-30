package com.mbti.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Builder.Default
    private Integer durationMinutes = 30;

    @Builder.Default
    private Integer questionLimit = 36;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exam_type_participant",
            joinColumns = @JoinColumn(name = "exam_type_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    @Builder.Default
    private List<User> participants = new ArrayList<>();

    @OneToMany(mappedBy = "examType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "examType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<DimensionDescription> dimensions = new ArrayList<>();

    @OneToMany(mappedBy = "examType", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<TestSession> sessions = new ArrayList<>();
}
