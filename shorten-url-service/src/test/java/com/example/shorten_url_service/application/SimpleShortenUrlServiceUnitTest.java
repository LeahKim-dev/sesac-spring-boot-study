package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.presentation.ShortenUrlCreateRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleShortenUrlServiceUnitTest {
    @Mock
    private ShortenUrlRepository shortenUrlRepository;
    @InjectMocks
    private SimpleShortenUrlService simpleShortenUrlService;
    @Test
    void 생성한_키가_모두_중복이면_예외가_발생한다() {
        ShortenUrlCreateRequestDto request =
                new ShortenUrlCreateRequestDto("https://www.hanbit.co.kr/");
        ShortenUrl existingUrl = new ShortenUrl("https://example.com/", "abc123");

        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(existingUrl);

        assertThrows(LackOfShortenUrlKeyException.class,
                () -> simpleShortenUrlService.generateShortenUrl(request));
    }
}
