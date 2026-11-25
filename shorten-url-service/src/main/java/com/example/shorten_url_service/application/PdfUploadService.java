package com.example.shorten_url_service.application;

import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import com.example.shorten_url_service.presentation.ShortenUrlCreateResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfUploadService {

    private final PdfFileStorage pdfFileStorage;
    private final SimpleShortenUrlService simpleShortenUrlService;

    public PdfUploadService(
            PdfFileStorage pdfFileStorage,
            SimpleShortenUrlService simpleShortenUrlService) {

        this.pdfFileStorage = pdfFileStorage;
        this.simpleShortenUrlService = simpleShortenUrlService;
    }

    public ShortenUrlCreateResponseDto upload(MultipartFile file) throws IOException {
        String fileName = pdfFileStorage.save(file);

        try {
            return simpleShortenUrlService.generatePdfShortenUrl(fileName);
        } catch (RuntimeException e) {
            try {
                pdfFileStorage.delete(fileName);
            } catch (IOException deleteException) {
                e.addSuppressed(deleteException);
            }
            throw e;
        }
    }

    public Resource getPdf(String fileName) {
        return pdfFileStorage.load(fileName);
    }
}