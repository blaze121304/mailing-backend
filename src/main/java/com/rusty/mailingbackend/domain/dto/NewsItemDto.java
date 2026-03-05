package com.rusty.mailingbackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsItemDto {
    private String category;
    private String title;
    private String body;
}