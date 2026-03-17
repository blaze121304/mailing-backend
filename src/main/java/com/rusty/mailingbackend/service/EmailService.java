package com.rusty.mailingbackend.service;

import com.rusty.mailingbackend.domain.dto.NewsItemDto;
import com.rusty.mailingbackend.domain.entity.NewsItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NewsGenerationService newsGenerationService;
    private final SubscriptionService subscriptionService;

    @Value("${newsletter.recipient}")
    private String testRecipientEmail;

    // 테스트 발송: 전체 15개를 고정 수신자에게
    public void sendTestNewsletter() throws MessagingException {
        log.info("테스트 뉴스레터 발송 시작 -> {}", testRecipientEmail);

        Map<String, Map<String, String>> newsData = newsGenerationService.getTodayNews();

        List<NewsItemDto> newsList = new ArrayList<>();
        for (String category : GeminiNewsService.CATEGORIES) {
            for (String difficulty : GeminiNewsService.DIFFICULTIES) {
                String raw = newsData.getOrDefault(category, Map.of()).getOrDefault(difficulty, "");
                newsList.add(new NewsItemDto(category, extractTitle(raw), extractBody(raw)));
            }
        }

        sendMail(testRecipientEmail, "[테스트] 오늘의 뉴스 - 5개 주제 x 3난이도", newsList);
        log.info("테스트 뉴스레터 발송 완료 -> {}", testRecipientEmail);
    }

    // 실제 발송: 구독자별 선택 카테고리 × 난이도 DB 조회 후 발송
    public void sendNewsletter() throws MessagingException {
        List<com.rusty.mailingbackend.domain.entity.Subscription> subscribers = subscriptionService.findAllSubscribers();

        log.info("실제 뉴스레터 발송 시작 - 구독자 {}명", subscribers.size());

        for (var subscriber : subscribers) {
            List<NewsItem> newsItems = newsGenerationService.fetchTodayNews(
                subscriber.getDifficulty(), subscriber.getCategories());
            List<NewsItemDto> newsList = toNewsItemDtoList(newsItems);
            if (newsList.isEmpty()) continue;

            sendMail(subscriber.getEmail(), "[뉴스레터] 오늘의 뉴스 - " + newsList.size() + "개 주제", newsList);
            log.info("발송 완료 -> {} ({}개 기사)", subscriber.getEmail(), newsList.size());
        }

        log.info("전체 발송 완료 - {}명", subscribers.size());
    }

    // 샘플 발송: 미구독자 전용, DB에서 조회하여 즉시 발송
    public void sendSampleNewsletter(String email, String difficulty, List<String> categories) throws MessagingException {
        if (subscriptionService.isSubscribed(email)) {
            throw new IllegalArgumentException("이미 구독 중인 이메일입니다. 구독자는 매일 오전 9시에 자동 발송됩니다.");
        }

        log.info("샘플 뉴스레터 발송 -> {} ({}개 카테고리, 난이도:{})", email, categories.size(), difficulty);

        List<NewsItem> newsItems = newsGenerationService.fetchTodayNews(difficulty, categories);
        List<NewsItemDto> newsList = toNewsItemDtoList(newsItems);

        sendMail(email, "[샘플] 오늘의 뉴스레터 - " + newsList.size() + "개 주제", newsList);
        log.info("샘플 뉴스레터 발송 완료 -> {}", email);
    }

    // 구독 신청 시 즉시 발송: DB에서 해당 난이도·카테고리 뉴스 조회하여 전달
    public void sendSubscriptionNews(String email, String difficulty, List<String> categories) throws MessagingException {
        List<NewsItem> newsItems = newsGenerationService.fetchTodayNews(difficulty, categories);
        if (newsItems.isEmpty()) {
            log.info("구독 완료 뉴스 발송 스킵 - 오늘 뉴스 없음 ({})", email);
            return;
        }
        List<NewsItemDto> newsList = toNewsItemDtoList(newsItems);
        sendMail(email, "[뉴스레터] 구독 완료! 오늘의 뉴스 - " + newsList.size() + "개 주제", newsList);
        log.info("구독 완료 뉴스 발송 완료 -> {}", email);
    }

    private List<NewsItemDto> toNewsItemDtoList(List<NewsItem> items) {
        return items.stream()
            .map(n -> new NewsItemDto(n.getCategory(), extractTitle(n.getContent()), extractBody(n.getContent())))
            .toList();
    }

    private void sendMail(String to, String subject, List<NewsItemDto> newsList) throws MessagingException {
        Context context = new Context();
        context.setVariable("newsList", newsList);
        String html = templateEngine.process("newsletter-full", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String extractTitle(String raw) {
        if (raw == null || !raw.contains("[제목]")) return raw;
        String after = raw.substring(raw.indexOf("[제목]") + 4);
        if (after.contains("[내용]")) after = after.substring(0, after.indexOf("[내용]"));
        return after.trim();
    }

    private String extractBody(String raw) {
        if (raw == null || !raw.contains("[내용]")) return "";
        return raw.substring(raw.indexOf("[내용]") + 4).trim();
    }
}