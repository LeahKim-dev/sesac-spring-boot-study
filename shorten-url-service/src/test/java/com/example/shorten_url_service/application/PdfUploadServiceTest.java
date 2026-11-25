package com.example.shorten_url_service.application;

import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

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
}
