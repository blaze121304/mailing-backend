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

    // 크론 주석처리 중 - 운영 시 해제: cron = "0 0 9 * * ?"  (매일 오전 9시)
    // @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyNewsletter() {
        try {
            // 1단계: DB에 오늘 뉴스 없으면 Gemini로 생성 후 저장
            newsGenerationService.getTodayNews();
            // 2단계: DB에서 꺼내서 발송
            emailService.sendNewsletter();
        } catch (MessagingException e) {
            log.error("뉴스레터 발송 실패: {}", e.getMessage());
        }
    }
}