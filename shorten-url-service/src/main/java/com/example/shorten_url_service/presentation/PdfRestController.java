package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.application.PdfUploadResult;
import com.example.shorten_url_service.application.PdfUploadService;
import com.example.shorten_url_service.domain.ShortenUrl;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
public class PdfRestController {

    private final PdfUploadService pdfUploadService;

    public PdfRestController(PdfUploadService pdfUploadService) {
        this.pdfUploadService = pdfUploadService;
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfUploadResponseDto upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        try {
            PdfUploadResult result = pdfUploadService.upload(file);
            ShortenUrl shortenUrl = result.shortenUrl();

            return new PdfUploadResponseDto(
                    shortenUrl.getOriginalUrl(),
                    shortenUrl.getShortenUrlKey(),
                    result.cancelToken()
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/pdf/{fileName}")
    public ResponseEntity<Void> cancel(
            @PathVariable String fileName,
            @RequestHeader("X-Cancel-Token") String cancelToken) throws IOException {

        if (!pdfUploadService.cancel(fileName, cancelToken)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pdf/{fileName}")
    public ResponseEntity<Resource> getPdf(
            @PathVariable String fileName) {

        Resource resource = pdfUploadService.getPdf(fileName);

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }
}