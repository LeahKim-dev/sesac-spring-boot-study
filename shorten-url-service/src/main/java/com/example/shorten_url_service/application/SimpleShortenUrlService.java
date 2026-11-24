package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.presentation.ShortenUrlCreateRequestDto;
import com.example.shorten_url_service.presentation.ShortenUrlCreateResponseDto;
import com.example.shorten_url_service.presentation.ShortenUrlInformationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Service
public class SimpleShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;

    // 서비스를 만들 때 ShortenUrlRepository를 구현한 객체를 넣어줘
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    @Transactional
    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto) {

        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        String shortenUrlKey = getUniqueShortenUrlKey();

//        2.원래의 URL과 단축 URL키를 통해 ShortenUrl도메인 객체 생성
        ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);

//        3.생성된 ShortenUrl을 레포지토리를 통해 저장
        shortenUrlRepository.insertShortenUrl(shortenUrl);

//        4.ShortenUrl을 ShortenUrlCreateResponseDto로 변환하여 반환
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);

        return shortenUrlCreateResponseDto;
    }

    private String getUniqueShortenUrlKey() {
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while(count++ < MAX_RETRY_COUNT) {
//            1.단축 URL 키(Key) 생성
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if (shortenUrl == null) {
                return shortenUrlKey;
            }
        }
        throw new LackOfShortenUrlKeyException();
    }

    @Transactional(readOnly = true)
    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        // 저장소야 shortenUrlKey에 해당하는 ShortenUrl 객체를 찾아줘
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        // 단축 URL을 찾지 못하는 상황
        if ( shortenUrl == null) {
            throw new NotFoundShortenUrlException();
        }

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    @Transactional
    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        // 단축 URL을 찾지 못하는 상황
        if ( shortenUrl == null) {
            throw new NotFoundShortenUrlException();
        }

        shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        return shortenUrl.getOriginalUrl();
    }
}
