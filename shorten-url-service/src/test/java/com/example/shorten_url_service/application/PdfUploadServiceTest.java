package com.example.shorten_url_service.application;

import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.presentation.ShortenUrlCreateResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PdfUploadServiceTest {

    @Test
    void removesFileWhenKeyIssuingFails() throws Exception {
        PdfFileStorage storage = mock(PdfFileStorage.class);
        SimpleShortenUrlService shortenUrlService = mock(SimpleShortenUrlService.class);
        PdfUploadService service = new PdfUploadService(storage, shortenUrlService);
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        RuntimeException failure = new IllegalStateException("database unavailable");

        when(storage.save(file)).thenReturn("pdf-123.pdf");
        when(shortenUrlService.generatePdfShortenUrl("pdf-123.pdf")).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> service.upload(file)));
        verify(storage).delete("pdf-123.pdf");
    }

    @Test
    void issuesCancelTokenWithThePdfCode() throws Exception {
        PdfFileStorage storage = mock(PdfFileStorage.class);
        SimpleShortenUrlService shortenUrlService = mock(SimpleShortenUrlService.class);
        PdfUploadService service = new PdfUploadService(storage, shortenUrlService);
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        String fileName = "pdf-123.pdf";
        String token = "12345678-1234-1234-1234-123456789abc";

        when(storage.save(file)).thenReturn(fileName);
        when(storage.createCancelToken(fileName)).thenReturn(token);
        when(shortenUrlService.generatePdfShortenUrl(fileName))
                .thenReturn(new ShortenUrlCreateResponseDto(new ShortenUrl("/pdf/" + fileName, "01234567")));

        var response = service.upload(file);
        assertEquals("01234567", response.shortenUrlKey());
        assertEquals("/pdf/" + fileName, response.originalUrl());
        assertEquals(token, response.cancelToken());
    }
}
