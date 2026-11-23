package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.application.SimpleShortenUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class ShortenUrlRestController {
    private final SimpleShortenUrlService simpleShortenUrlService;

    @Autowired
    public ShortenUrlRestController(SimpleShortenUrlService simpleShortenUrlService) {
        this.simpleShortenUrlService = simpleShortenUrlService;
    }
//    1. 단축 URL 생성 API
    @PostMapping("/shortenUrl")
    public ShortenUrlCreateResponseDto createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
            ) {
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = simpleShortenUrlService.generateShortenUrl(shortenUrlCreateRequestDto);
        return shortenUrlCreateResponseDto;
    }

//    2. 단축 URL 리다이렉트 API
    @GetMapping("/{shortenUrlKey}")
    public ResponseEntity<?> redirectShortenUrl(
            @PathVariable String shortenUrlKey
    ) throws URISyntaxException {
        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        URI redirectUri = new URI(originalUrl); // 문자열을 URI객체로
        
        HttpHeaders httpHeaders = new HttpHeaders(); // 서버가 보낼 헤더를 담는 객체
        httpHeaders.setLocation(redirectUri); // 응답 헤더의 Location 항목에 그 주소를 넣기

        return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY); // 301
    }

//    3. 단축 URL 정보 조회 API
    @GetMapping("/shortenUrl/{shortenUrlKey}")
    public ShortenUrlInformationDto getShortenUrlInformation(
            @PathVariable("shortenUrlKey") String shortenUrlKey
    ) {
        ShortenUrlInformationDto shortenUrlInformationDto = simpleShortenUrlService.getShortenUrlInformationByShortenUrlKey(shortenUrlKey);

        return shortenUrlInformationDto;
    }

}
