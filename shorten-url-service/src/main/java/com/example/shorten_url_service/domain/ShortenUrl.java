package com.example.shorten_url_service.domain;

import java.security.SecureRandom;
import java.util.Random;

public class ShortenUrl {
    private String originalUrl;
    private String shortenUrlKey;
    private Long redirectCount;
    private static final SecureRandom RANDOM = new SecureRandom();

    public ShortenUrl(String originalUrl, String shortenUrlKey) {
        this(originalUrl, shortenUrlKey, 0L);
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey, Long redirectCount) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectCount() {
        return redirectCount;
    }

    public void increaseRedirectCount() {
        redirectCount++;
    }

    public static String generateShortenUrlKey() {
        String base56Characters = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";

        Random random = new Random();
        StringBuilder shortenUrlKey = new StringBuilder(); // StringBuilder는 스레드 세이프하지 않지만 메서드 내 지역변수가 해당 스레드 내에서만 사용되기 때문에 괜찮음
        // 각 지역변수는 각 스레드의 스택에 저장, 다른 스레드에 공유되지 않음

        // 반복문이 돌때마다 랜덤 위치를 뽑고 그 위치의 문자를 꺼내고 키뒤에 붙임
        for (int count = 0; count < 8; count++) {
            int digit = RANDOM.nextInt(10);
            shortenUrlKey.append(digit);
        }
        return shortenUrlKey.toString();
    }
}
