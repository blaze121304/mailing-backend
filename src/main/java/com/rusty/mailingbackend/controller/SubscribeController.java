package com.rusty.mailingbackend.controller;

import com.rusty.mailingbackend.domain.dto.SubscribeRequest;
import com.rusty.mailingbackend.service.EmailService;
import com.rusty.mailingbackend.service.NewsGenerationService;
import com.rusty.mailingbackend.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscribeController {

    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final NewsGenerationService newsGenerationService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscribeRequest request) {
        subscriptionService.subscribe(request);
        return ResponseEntity.ok().build();
    }

    // DB에서 오늘 뉴스 꺼내서 발송 (없으면 Gemini 생성 후 저장)
    @PostMapping("/send-newsletter")
    public ResponseEntity<String> sendNewsletter() {
        try {
            emailService.sendNewsletter();
            return ResponseEntity.ok("뉴스레터 발송 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("발송 실패: " + e.getMessage());
        }
    }

    // 뉴스 raw 내용 확인용 (디버그)
    @GetMapping("/debug-news")
    public ResponseEntity<java.util.Map<String, java.util.Map<String, String>>> debugNews() {
        return ResponseEntity.ok(newsGenerationService.getTodayNews());
    }

    // 오늘 뉴스 강제 재생성 (테스트용)
    @PostMapping("/regenerate-news")
    public ResponseEntity<String> regenerateNews() {
        try {
            newsGenerationService.regenerateToday();
            return ResponseEntity.ok("뉴스 재생성 완료! /send-newsletter 로 발송하세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("재생성 실패: " + e.getMessage());
        }
    }
}