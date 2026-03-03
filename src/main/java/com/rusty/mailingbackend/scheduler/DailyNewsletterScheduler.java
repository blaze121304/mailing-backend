package com.rusty.mailingbackend.scheduler;

import com.rusty.mailingbackend.entity.Subscription;
import com.rusty.mailingbackend.service.EmailService;
import com.rusty.mailingbackend.service.SubscriptionService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyNewsletterScheduler {

    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    // 테스트용: 1분마다 실행. 운영 시 예: cron = "0 0 9 * * ?" (매일 9시)
    @Scheduled(cron = "0 * * * * ?")
    public void sendNewsletters() {
        List<Subscription> subscribers = subscriptionService.findAllSubscribers();
        for (Subscription sub : subscribers) {
            try {
                String html = emailService.renderNewsletterTemplate(sub.getCategories());
                emailService.sendNewsletter(sub.getEmail(), sub.getCategories(), html);
                log.info("발송 완료: {}", sub.getEmail());
            } catch (MessagingException e) {
                log.warn("발송 실패: {} - {}", sub.getEmail(), e.getMessage());
            }
        }
    }
}
