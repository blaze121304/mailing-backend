package com.rusty.mailingbackend.repository;

import com.rusty.mailingbackend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByEmail(String email);

    List<Subscription> findAll();
}
