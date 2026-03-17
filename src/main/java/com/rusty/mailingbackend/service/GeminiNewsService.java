package com.rusty.mailingbackend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiNewsService {

    private final RestTemplate restTemplate;
    private final ResourceLoader resourceLoader;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public static final List<String> CATEGORIES = List.of("사회", "경제", "정치", "IT&기술", "문화");
    public static final List<String> DIFFICULTIES = List.of("하", "중", "상");

    private Map<String, Map<String, String>> prompts;

    @PostConstruct
    private void loadPrompts() {
        prompts = new HashMap<>();
        for (String category : CATEGORIES) {
            try {
                Resource resource = resourceLoader.getResource("classpath:prompts/" + category + ".md");
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                prompts.put(category, parsePromptFile(content));
            } catch (IOException e) {
                log.warn("프롬프트 파일 로드 실패 [{}]: {}", category, e.getMessage());
                prompts.put(category, Map.of());
            }
        }
    }

    private Map<String, String> parsePromptFile(String content) {
        Map<String, String> result = new HashMap<>();
        String[] sections = content.split("(?m)^## ");
        for (String section : sections) {
            if (section.isBlank()) continue;
            int newline = section.indexOf('\n');
            if (newline < 0) continue;
            String difficulty = section.substring(0, newline).trim();
            String prompt = section.substring(newline + 1).strip();
            result.put(difficulty, prompt);
        }
        return result;
    }

    public String generateNews(String category, String difficulty) {
        String prompt = prompts.getOrDefault(category, Map.of())
                               .getOrDefault(difficulty, category + " 분야 최신 뉴스를 한국어로 요약해주세요.");

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of("thinkingConfig", Map.of("thinkingBudget", 0))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL + apiKey, request, Map.class);
            return extractText(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API 실패 [{}][{}]: {}", category, difficulty, e.getMessage());
            return "[%s][%s] 뉴스 생성 실패".formatted(category, difficulty);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> body) {
        try {
            List<?> candidates = (List<?>) body.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            // thinking 파트(thought=true)를 건너뛰고 실제 텍스트 파트를 반환
            for (Object part : parts) {
                Map<?, ?> partMap = (Map<?, ?>) part;
                if (Boolean.TRUE.equals(partMap.get("thought"))) continue;
                return (String) partMap.get("text");
            }
            return "뉴스 생성에 실패했습니다.";
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            return "뉴스 생성에 실패했습니다.";
        }
    }
}