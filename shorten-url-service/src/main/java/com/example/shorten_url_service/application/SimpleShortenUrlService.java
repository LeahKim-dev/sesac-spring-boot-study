package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.presentation.ShortenUrlCreateRequestDto;
import com.example.shorten_url_service.presentation.ShortenUrlCreateResponseDto;
import com.example.shorten_url_service.presentation.ShortenUrlInformationDto;
import org.springframework.dao.DataIntegrityViolationException;
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

    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto) {

        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();

        ShortenUrl shortenUrl = createAndSaveShortenUrl(originalUrl);

        return new ShortenUrlCreateResponseDto(shortenUrl);
    }

    private ShortenUrl createAndSaveShortenUrl(String originalUrl) {
        final int MAX_RETRY_COUNT = 5;

        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();

            ShortenUrl existingUrl =
                    shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if (existingUrl != null) {
                continue;
            }

            ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);

            try {
                shortenUrlRepository.insertShortenUrl(shortenUrl);
                return shortenUrl;
            } catch (DataIntegrityViolationException ex) {
                if (shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey) == null) {
                    throw ex;
                }
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
