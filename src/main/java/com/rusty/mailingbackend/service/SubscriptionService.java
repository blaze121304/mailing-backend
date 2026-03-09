package com.rusty.mailingbackend.service;

import com.rusty.mailingbackend.domain.dto.SubscribeRequest;
import com.rusty.mailingbackend.domain.entity.Subscription;
import com.rusty.mailingbackend.common.exception.DuplicateEmailException;
import com.rusty.mailingbackend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void subscribe(SubscribeRequest request) {
        if (subscriptionRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 구독 중인 이메일입니다: " + request.getEmail());
        }
        Subscription subscription = Subscription.builder()
                .email(request.getEmail())
                .difficulty(request.getDifficulty())
                .categories(request.getCategories())
                .build();
        subscriptionRepository.save(subscription);
    }

    public List<Subscription> findAllSubscribers() {
        return subscriptionRepository.findAll();
    }

    public boolean isSubscribed(String email) {
        return subscriptionRepository.existsByEmail(email);
    }

    public Subscription findByEmail(String email) {
        return subscriptionRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("구독 정보를 찾을 수 없는 이메일입니다: " + email));
    }
}
