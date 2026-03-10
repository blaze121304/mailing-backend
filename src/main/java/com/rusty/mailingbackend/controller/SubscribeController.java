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

    // 실제 구독자 발송: 구독자별 선택 카테고리 × 난이도 1개씩 (1~5개)
    @PostMapping("/send-newsletter")
    public ResponseEntity<String> sendNewsletter() {
        try {
            emailService.sendNewsletter();
            return ResponseEntity.ok("뉴스레터 발송 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("발송 실패: " + e.getMessage());
        }
    }

    // 샘플 발송: 미구독자 전용, body로 이메일·난이도·카테고리 입력
    @PostMapping("/sample-newsletter")
    public ResponseEntity<String> sampleNewsletter(@Valid @RequestBody SubscribeRequest request) {
        try {
            emailService.sendSampleNewsletter(request.getEmail(), request.getDifficulty(), request.getCategories());
            return ResponseEntity.ok("샘플 뉴스레터 발송 완료!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("발송 실패: " + e.getMessage());
        }
    }

    // 테스트 발송: 전체 15개를 고정 수신자에게
    @PostMapping("/test-send-newsletter")
    public ResponseEntity<String> testSendNewsletter() {
        try {
            emailService.sendTestNewsletter();
            return ResponseEntity.ok("테스트 뉴스레터 발송 완료!");
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

    @GetMapping("/mailing")
    @ResponseBody
    public String health() {
        return "OK";
    }
}