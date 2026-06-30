package com.mbti.service;

import com.mbti.entity.Answer;
import com.mbti.entity.QuestionOption;
import com.mbti.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MbtiScoringService {

    private final QuestionOptionRepository questionOptionRepository;

    private static final List<String> MBTI_KEYS = List.of("E", "I", "S", "N", "T", "F", "J", "P");

    public Map<String, Integer> calculateScores(List<Answer> answers) {
        // Initialize scores
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String key : MBTI_KEYS) {
            scores.put(key, 0);
        }

        // Get all option IDs
        List<String> optionIds = answers.stream()
                .map(Answer::getOptionId)
                .toList();

        if (optionIds.isEmpty()) {
            return scores;
        }

        // Fetch options and accumulate scores
        List<QuestionOption> options = questionOptionRepository.findByIdIn(optionIds);
        for (QuestionOption option : options) {
            String dimension = option.getDimension();
            if (scores.containsKey(dimension)) {
                scores.merge(dimension, option.getWeight(), Integer::sum);
            }
        }

        return scores;
    }

    public String buildMbtiType(Map<String, Integer> scores) {
        String ei = scores.getOrDefault("E", 0) >= scores.getOrDefault("I", 0) ? "E" : "I";
        String sn = scores.getOrDefault("S", 0) >= scores.getOrDefault("N", 0) ? "S" : "N";
        String tf = scores.getOrDefault("T", 0) >= scores.getOrDefault("F", 0) ? "T" : "F";
        String jp = scores.getOrDefault("J", 0) >= scores.getOrDefault("P", 0) ? "J" : "P";
        return ei + sn + tf + jp;
    }
}
