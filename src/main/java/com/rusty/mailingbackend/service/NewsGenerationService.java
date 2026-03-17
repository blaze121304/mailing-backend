package com.rusty.mailingbackend.service;

import com.rusty.mailingbackend.domain.entity.NewsItem;
import com.rusty.mailingbackend.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsGenerationService {

    private final GeminiNewsService geminiNewsService;
    private final NewsItemRepository newsItemRepository;

    /**
     * 오전 8시 스케줄러용: DB에 오늘 뉴스 없으면 Gemini로 15개 생성 후 저장
     */
    public void generateTodayNews() {
        LocalDate today = LocalDate.now();
        List<NewsItem> existing = newsItemRepository.findByNewsDate(today);

        if (existing.size() < 15) {
            log.info("오늘 뉴스 생성 시작 (현재 {}개)", existing.size());
            generateAndSave(today);
        } else {
            log.info("오늘 뉴스 이미 생성됨 ({}개), 스킵", existing.size());
        }
    }

    /**
     * DB에서 오늘 뉴스를 난이도·카테고리로 조회 (생성하지 않음)
     */
    public List<NewsItem> fetchTodayNews(String difficulty, List<String> categories) {
        return newsItemRepository.findByNewsDateAndDifficultyAndCategoryIn(
            LocalDate.now(), difficulty, categories);
    }

    /**
     * 오늘 뉴스가 DB에 없으면 Gemini로 15개 생성 후 저장, 있으면 그대로 반환 (debug/test용)
     */
    public Map<String, Map<String, String>> getTodayNews() {
        LocalDate today = LocalDate.now();
        List<NewsItem> existing = newsItemRepository.findByNewsDate(today);

        if (existing.size() < 15) {
            log.info("오늘 뉴스 부족 ({}개), Gemini로 신규 생성 시작...", existing.size());
            generateAndSave(today);
            existing = newsItemRepository.findByNewsDate(today);
        } else {
            log.info("DB에서 오늘 뉴스 {}개 로드", existing.size());
        }

        return toNewsMap(existing);
    }

    /**
     * 강제 재생성 (테스트용)
     */
    public Map<String, Map<String, String>> regenerateToday() {
        LocalDate today = LocalDate.now();
        newsItemRepository.deleteAll(newsItemRepository.findByNewsDate(today));
        generateAndSave(today);
        return toNewsMap(newsItemRepository.findByNewsDate(today));
    }

    private void generateAndSave(LocalDate date) {
        for (String category : GeminiNewsService.CATEGORIES) {
            for (String difficulty : GeminiNewsService.DIFFICULTIES) {
                if (newsItemRepository.existsByNewsDateAndCategoryAndDifficulty(date, category, difficulty)) {
                    log.info("이미 존재: [{}][{}], 스킵", category, difficulty);
                    continue;
                }
                log.info("생성 중: [{}][{}]", category, difficulty);
                String content = geminiNewsService.generateNews(category, difficulty);
                newsItemRepository.save(NewsItem.builder()
                    .newsDate(date)
                    .category(category)
                    .difficulty(difficulty)
                    .content(content)
                    .build());
            }
        }
        log.info("{}일자 뉴스 DB 저장 완료", date);
    }

    private Map<String, Map<String, String>> toNewsMap(List<NewsItem> items) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (String category : GeminiNewsService.CATEGORIES) {
            Map<String, String> diffMap = new LinkedHashMap<>();
            for (String difficulty : GeminiNewsService.DIFFICULTIES) {
                items.stream()
                    .filter(n -> n.getCategory().equals(category) && n.getDifficulty().equals(difficulty))
                    .findFirst()
                    .ifPresent(n -> diffMap.put(difficulty, n.getContent()));
            }
            result.put(category, diffMap);
        }
        return result;
    }
}