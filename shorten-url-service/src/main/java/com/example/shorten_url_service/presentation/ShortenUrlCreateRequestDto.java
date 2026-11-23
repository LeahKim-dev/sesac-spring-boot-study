package com.example.shorten_url_service.presentation;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

// 생성 요청 DTO
// 스프링이 요청 JSON을 읽을 때 원본 주소 받기
public class ShortenUrlCreateRequestDto {
    @NotNull
    @URL(regexp = "(?i)https?://.+")
    private String originalUrl; // 사용자가 보낸 값(원본 주소)

    public ShortenUrlCreateRequestDto() {
    }

    public ShortenUrlCreateRequestDto(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    public String getOriginalUrl() {
        return originalUrl;
    }
}
