package com.mbti.controller.admin;

import com.mbti.dto.request.DimensionUpdateRequest;
import com.mbti.entity.DimensionDescription;
import com.mbti.repository.DimensionDescriptionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dimensions")
@RequiredArgsConstructor
public class AdminDimensionController {

    private final DimensionDescriptionRepository dimensionRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String examTypeId) {
        List<DimensionDescription> dimensions;
        if (examTypeId != null && !examTypeId.isEmpty()) {
            dimensions = dimensionRepository.findByExamTypeIdOrderByCodeAsc(examTypeId);
        } else {
            dimensions = dimensionRepository.findAll();
        }
        return ResponseEntity.ok(Map.of("dimensions", dimensions));
    }

    @PutMapping
    public ResponseEntity<?> upsert(@Valid @RequestBody DimensionUpdateRequest request) {
        DimensionDescription dimension = dimensionRepository
                .findByExamTypeIdAndCode(request.getExamTypeId(), request.getCode())
                .orElse(null);

        if (dimension != null) {
            dimension.setName(request.getName());
            dimension.setDescription(request.getDescription());
        } else {
            dimension = DimensionDescription.builder()
                    .code(request.getCode())
                    .name(request.getName())
                    .description(request.getDescription())
                    .examTypeId(request.getExamTypeId())
                    .build();
        }

        DimensionDescription saved = dimensionRepository.save(dimension);
        return ResponseEntity.ok(Map.of("ok", true, "dimension", saved));
    }
}
