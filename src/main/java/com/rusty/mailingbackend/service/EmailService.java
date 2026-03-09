package com.rusty.mailingbackend.service;

import com.rusty.mailingbackend.domain.dto.NewsItemDto;
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

    // 실제 발송: 구독자별 선택 카테고리 × 난이도 1개씩 (1~5개)
    public void sendNewsletter() throws MessagingException {
        Map<String, Map<String, String>> newsData = newsGenerationService.getTodayNews();
        List<com.rusty.mailingbackend.domain.entity.Subscription> subscribers = subscriptionService.findAllSubscribers();

        log.info("실제 뉴스레터 발송 시작 - 구독자 {}명", subscribers.size());

        for (var subscriber : subscribers) {
            List<NewsItemDto> newsList = new ArrayList<>();
            for (String category : subscriber.getCategories()) {
                String raw = newsData.getOrDefault(category, Map.of())
                                     .getOrDefault(subscriber.getDifficulty(), "");
                newsList.add(new NewsItemDto(category, extractTitle(raw), extractBody(raw)));
            }
            if (newsList.isEmpty()) continue;

            sendMail(subscriber.getEmail(), "[뉴스레터] 오늘의 뉴스 - " + newsList.size() + "개 주제", newsList);
            log.info("발송 완료 -> {} ({}개 기사)", subscriber.getEmail(), newsList.size());
        }

        log.info("전체 발송 완료 - {}명", subscribers.size());
    }

    // 샘플 발송: 미구독자 전용, DB 저장 없이 즉시 발송
    public void sendSampleNewsletter(String email, String difficulty, List<String> categories) throws MessagingException {
        if (subscriptionService.isSubscribed(email)) {
            throw new IllegalArgumentException("이미 구독 중인 이메일입니다. 구독자는 매일 오전 9시에 자동 발송됩니다.");
        }

        log.info("샘플 뉴스레터 발송 시작 -> {} ({}개 카테고리, 난이도:{})", email, categories.size(), difficulty);

        Map<String, Map<String, String>> newsData = newsGenerationService.getTodayNews();

        List<NewsItemDto> newsList = new ArrayList<>();
        for (String category : categories) {
            String raw = newsData.getOrDefault(category, Map.of()).getOrDefault(difficulty, "");
            newsList.add(new NewsItemDto(category, extractTitle(raw), extractBody(raw)));
        }

        sendMail(email, "[샘플] 오늘의 뉴스레터 - " + newsList.size() + "개 주제", newsList);
        log.info("샘플 뉴스레터 발송 완료 -> {}", email);
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