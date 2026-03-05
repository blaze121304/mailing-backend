package com.rusty.mailingbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiNewsService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public static final List<String> CATEGORIES = List.of("사회", "경제", "정치", "IT&기술", "문화");
    public static final List<String> DIFFICULTIES = List.of("하", "중", "상");

    private static final Map<String, Map<String, String>> PROMPTS = Map.of(
        "사회", Map.of(
            "하", "당신은 친근한 뉴스 큐레이터입니다.\n최근 우리 사회에서 일어난 흥미롭고 공감 가는 생활밀착형 사건이나 사회현상을 하나 골라, 전문용어 없이 중학생도 이해할 수 있게 쉽고 재미있게 설명해주세요. 딱딱하지 않게, 친구한테 얘기해주듯 써주세요.\n형식:\n[제목] 제목\n[내용] 3~4문장 본문",
            "중", "당신은 뉴스 큐레이터입니다.\n최근 한국 사회에서 주목받는 중요한 사회 이슈를 하나 선정하여, 배경과 현황, 사회적 의미를 일반 성인이 이해할 수 있는 수준으로 정리해주세요.\n형식:\n[제목] 제목\n[내용] 3~5문장 본문",
            "상", "당신은 전문 사회 분석가입니다.\n최근 한국 사회의 구조적 변화나 심층적인 사회문제(인구구조 변화, 계층 이동, 사회 양극화 등)를 분석적 시각으로 전문적으로 서술해주세요. 해당 분야에 관심이 깊은 독자가 인사이트를 얻을 수 있도록 심층적으로 작성해주세요.\n형식:\n[제목] 제목\n[내용] 4~6문장 본문"
        ),
        "경제", Map.of(
            "하", "당신은 친근한 경제 해설사입니다.\n최근 경제 뉴스 중 일상생활과 직결되는 쉬운 내용(물가, 소비 트렌드, 생활비 등)을 골라 경제 용어 없이 누구나 이해할 수 있게 풀어서 설명해주세요.\n형식:\n[제목] 제목\n[내용] 3~4문장 본문",
            "중", "당신은 경제 뉴스 큐레이터입니다.\n최근 국내외 경제 동향 중 주목할 만한 이슈를 하나 선정하여, 주요 지표와 배경을 포함해 일반 성인 수준에서 이해할 수 있도록 요약해주세요.\n형식:\n[제목] 제목\n[내용] 3~5문장 본문",
            "상", "당신은 전문 경제 애널리스트입니다.\n최근 글로벌 또는 국내 거시경제 흐름, 금융시장 동향, 통화정책 등 전문적인 경제 이슈를 심층 분석하여 경제에 조예가 깊은 독자가 인사이트를 얻을 수 있도록 작성해주세요.\n형식:\n[제목] 제목\n[내용] 4~6문장 본문"
        ),
        "정치", Map.of(
            "하", "당신은 친근한 정치 해설사입니다.\n최근 정치 뉴스 중 복잡한 정치 배경 없이도 쉽게 이해할 수 있는 내용을 골라, 왜 중요한지 일상 언어로 재미있게 설명해주세요.\n형식:\n[제목] 제목\n[내용] 3~4문장 본문",
            "중", "당신은 정치 뉴스 큐레이터입니다.\n최근 국내 정치 현안 중 주요 이슈를 하나 선정하여, 각 입장과 배경을 균형 있게 정리하여 일반 성인이 이해할 수 있도록 요약해주세요.\n형식:\n[제목] 제목\n[내용] 3~5문장 본문",
            "상", "당신은 전문 정치 분석가입니다.\n최근 국내외 정치 지형의 변화, 정책 논쟁, 권력 구조 분석 등을 정치에 관심이 깊은 독자가 심층적으로 파악할 수 있도록 전문적으로 서술해주세요.\n형식:\n[제목] 제목\n[내용] 4~6문장 본문"
        ),
        "IT&기술", Map.of(
            "하", "당신은 친근한 IT 해설사입니다.\n최근 IT·기술 뉴스 중 일상에서 쉽게 접할 수 있는 신기한 기술이나 앱, 트렌드를 골라 기술 지식이 없어도 누구나 흥미롭게 읽을 수 있도록 설명해주세요.\n형식:\n[제목] 제목\n[내용] 3~4문장 본문",
            "중", "당신은 IT 뉴스 큐레이터입니다.\n최근 IT·기술 분야의 주목할 만한 이슈나 신기술 동향을 하나 선정하여, 기본적인 IT 지식을 가진 독자가 이해할 수 있도록 핵심을 정리해주세요.\n형식:\n[제목] 제목\n[내용] 3~5문장 본문",
            "상", "당신은 IT 업계 전문 애널리스트입니다.\n최근 IT·기술 업계의 심층 트렌드(AI, 반도체, 클라우드, 양자컴퓨팅 등)나 기술적 혁신을 해당 분야 전문가 또는 깊은 관심을 가진 독자가 전문 지식을 파악할 수 있도록 상세히 서술해주세요.\n형식:\n[제목] 제목\n[내용] 4~6문장 본문"
        ),
        "문화", Map.of(
            "하", "당신은 트렌디한 문화 큐레이터입니다.\n최근 대중문화, 엔터테인먼트, 유행 트렌드 중 가볍고 재미있는 소식을 골라 누구나 즐겁게 읽을 수 있도록 친근한 언어로 소개해주세요.\n형식:\n[제목] 제목\n[내용] 3~4문장 본문",
            "중", "당신은 문화 뉴스 큐레이터입니다.\n최근 문화·예술·미디어 분야에서 주목받는 이슈나 트렌드를 하나 선정하여, 문화적 의미와 사회적 배경을 포함해 정리해주세요.\n형식:\n[제목] 제목\n[내용] 3~5문장 본문",
            "상", "당신은 문화 평론가입니다.\n최근 문화 현상, 예술계 동향, 미디어 산업의 구조적 변화 등을 문화에 깊은 관심을 가진 독자가 심층적으로 이해할 수 있도록 분석적으로 서술해주세요.\n형식:\n[제목] 제목\n[내용] 4~6문장 본문"
        )
    );

    public String generateNews(String category, String difficulty) {
        String prompt = PROMPTS.getOrDefault(category, Map.of())
                               .getOrDefault(difficulty, category + " 분야 최신 뉴스를 한국어로 요약해주세요.");

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
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
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            return "뉴스 생성에 실패했습니다.";
        }
    }
}