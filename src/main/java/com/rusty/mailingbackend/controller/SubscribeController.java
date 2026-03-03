package com.rusty.mailingbackend.controller;

import com.rusty.mailingbackend.dto.SubscribeRequest;
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

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscribeRequest request) {
        subscriptionService.subscribe(request);
        return ResponseEntity.ok().build();
    }
}
