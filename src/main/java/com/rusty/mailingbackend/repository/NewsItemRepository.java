package com.rusty.mailingbackend.repository;

import com.rusty.mailingbackend.domain.entity.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    List<NewsItem> findByNewsDate(LocalDate date);

    boolean existsByNewsDateAndCategoryAndDifficulty(LocalDate date, String category, String difficulty);
}