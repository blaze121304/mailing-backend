package com.rusty.mailingbackend.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyNewsletterScheduler {

    private final NewsGenerationService newsGenerationService;
    private final EmailService emailService;

    @Scheduled(cron = "0 1 0 * * ?")
    public void generateDailyNews() {
        log.info("일간 뉴스 생성 시작");
        newsGenerationService.generateTodayNews();
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyNewsletter() {
        try {
            log.info("일간 뉴스레터 발송 시작");
            emailService.sendNewsletter();
        } catch (MessagingException e) {
            log.error("뉴스레터 발송 실패: {}", e.getMessage());
        }
    }
}