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

    @Value("${newsletter.recipient}")
    private String recipientEmail;

    public void sendNewsletter() throws MessagingException {
        log.info("뉴스레터 발송 시작 -> {}", recipientEmail);

        Map<String, Map<String, String>> newsData = newsGenerationService.getTodayNews();

        List<NewsItemDto> newsList = new ArrayList<>();
        for (String category : GeminiNewsService.CATEGORIES) {
            for (String difficulty : GeminiNewsService.DIFFICULTIES) {
                String raw = newsData.getOrDefault(category, Map.of()).getOrDefault(difficulty, "");
                String title = extractTitle(raw);
                String body  = extractBody(raw);
                newsList.add(new NewsItemDto(category, title, body));
            }
        }

        Context context = new Context();
        context.setVariable("newsList", newsList);

        String html = templateEngine.process("newsletter-full", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(recipientEmail);
        helper.setSubject("[뉴스레터] 오늘의 뉴스 - 5개 주제 x 3난이도");
        helper.setText(html, true);
        mailSender.send(message);

        log.info("뉴스레터 발송 완료 -> {}", recipientEmail);
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