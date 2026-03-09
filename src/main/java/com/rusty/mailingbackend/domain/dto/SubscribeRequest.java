package com.rusty.mailingbackend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotEmpty(message = "이메일은 필수입니다")
    private String email;

    @NotEmpty(message = "난이도는 필수입니다")
    private String difficulty;

    @NotEmpty(message = "카테고리는 1개 이상 선택해야 합니다")
    private List<String> categories;
}

