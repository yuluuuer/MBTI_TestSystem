package com.mbti.controller.admin;

import com.mbti.dto.response.StatsResponse;
import com.mbti.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<?> getStats(@RequestParam(required = false) String examTypeId) {
        StatsResponse stats = statsService.getStats(examTypeId);
        return ResponseEntity.ok(stats);
    }
}
