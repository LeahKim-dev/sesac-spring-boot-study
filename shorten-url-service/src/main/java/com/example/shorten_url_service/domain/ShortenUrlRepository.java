package com.example.shorten_url_service.domain;

// 저장과 조회에 사용할 메서드 선언 인터페이스
public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);

    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);
}
