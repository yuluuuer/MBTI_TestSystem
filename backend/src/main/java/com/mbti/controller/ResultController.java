package com.mbti.controller;

import com.mbti.entity.TestResult;
import com.mbti.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
public class ResultController {

    private final TestResultRepository resultRepository;

    private static final Map<String, String> DESC_MAP = Map.ofEntries(
            Map.entry("INTJ", "理性且有战略眼光，善于系统性解决复杂问题。"),
            Map.entry("INTP", "好奇且独立思考，擅长抽象分析与创新。"),
            Map.entry("ENTJ", "目标导向，执行力强，偏好高效推进。"),
            Map.entry("ENTP", "思维灵活，喜欢探索新点子并挑战常规。"),
            Map.entry("INFJ", "重视意义与价值，洞察力强且关注他人。"),
            Map.entry("INFP", "理想主义，重视内在价值和真实表达。"),
            Map.entry("ENFJ", "善于共情与组织，擅长激励团队。"),
            Map.entry("ENFP", "热情开放，充满创意并善于连接人。"),
            Map.entry("ISTJ", "稳健务实，注重规则与责任落实。"),
            Map.entry("ISFJ", "细致可靠，重视秩序和对他人的支持。"),
            Map.entry("ESTJ", "组织能力强，偏好清晰流程和高标准。"),
            Map.entry("ESFJ", "关注协作与和谐，擅长维持团队氛围。"),
            Map.entry("ISTP", "冷静务实，擅长快速定位问题并处理。"),
            Map.entry("ISFP", "温和灵活，重视体验与个人价值感受。"),
            Map.entry("ESTP", "行动力强，适应变化并擅长现场应对。"),
            Map.entry("ESFP", "外向活跃，重视体验并善于带动氛围。")
    );

    @GetMapping("/{id}")
    public ResponseEntity<?> getResult(@PathVariable String id) {
        TestResult result = resultRepository.findById(id).orElse(null);
        if (result == null) {
            return ResponseEntity.status(404).body(Map.of("message", "结果不存在"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", result.getId());
        response.put("mbtiType", result.getMbtiType());
        response.put("description", DESC_MAP.getOrDefault(result.getMbtiType(),
                "这是你的当前倾向类型，可结合详细维度分数进一步分析。"));
        response.put("scoresJson", result.getScoresJson());
        response.put("createdAt", result.getCreatedAt() != null ? result.getCreatedAt().toString() : null);

        return ResponseEntity.ok(response);
    }
}
