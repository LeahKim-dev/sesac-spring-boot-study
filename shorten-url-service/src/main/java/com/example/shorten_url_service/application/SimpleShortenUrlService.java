package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import com.example.shorten_url_service.presentation.ShortenUrlCreateResponseDto;
import com.example.shorten_url_service.presentation.ShortenUrlInformationDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimpleShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;
    private final PdfFileStorage pdfFileStorage;

    // 서비스를 만들 때 ShortenUrlRepository를 구현한 객체를 넣어줘
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository, PdfFileStorage pdfFileStorage) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.pdfFileStorage = pdfFileStorage;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(
            String originalUrl) {

        ShortenUrl shortenUrl = createAndSaveShortenUrl(originalUrl);

        return new ShortenUrlCreateResponseDto(shortenUrl);
    }

    public ShortenUrlCreateResponseDto generatePdfShortenUrl(String fileName) {
        String originalUrl = "/pdf/" + fileName;

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

    private ShortenUrl findAvailableShortenUrl(String shortenUrlKey) {
        ShortenUrl shortenUrl =
                shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (shortenUrl == null) {
            throw new NotFoundShortenUrlException();
        }

        String originalUrl = shortenUrl.getOriginalUrl();

        if (originalUrl.startsWith("/pdf/")) {
            String fileName = originalUrl.substring("/pdf/".length());

            if (pdfFileStorage.load(fileName) == null) {
                throw new NotFoundShortenUrlException();
            }
        }

        return shortenUrl;
    }

    @Transactional(readOnly = true)
    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = findAvailableShortenUrl(shortenUrlKey);

        return new ShortenUrlInformationDto(shortenUrl);
    }

    @Transactional
    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = findAvailableShortenUrl(shortenUrlKey);

        shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        return shortenUrl.getOriginalUrl();
    }
}
