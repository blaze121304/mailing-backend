package com.rusty.mailingbackend.repository;

import com.rusty.mailingbackend.domain.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findByArticleDate(LocalDate date);

    Optional<NewsArticle> findByArticleDateAndCategoryAndDifficulty(
            LocalDate date, String category, String difficulty);

    boolean existsByArticleDate(LocalDate date);
}