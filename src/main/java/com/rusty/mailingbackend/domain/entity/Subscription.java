package com.rusty.mailingbackend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "varchar(255) default '하'")
    private String difficulty;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_categories", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "category")
    @Builder.Default
    private List<String> categories = new ArrayList<>();
}
