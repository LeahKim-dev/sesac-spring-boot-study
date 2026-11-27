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
import java.util.List;
import org.springframework.web.util.HtmlUtils;
import com.example.shorten_url_service.domain.SavedPdfFile;

@RestController
public class PdfRestController {

    private final PdfUploadService pdfUploadService;

    public PdfRestController(PdfUploadService pdfUploadService) {
        this.pdfUploadService = pdfUploadService;
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfUploadResponseDto upload(
            @RequestParam("file") List<MultipartFile> files) throws IOException {

        try {
            PdfUploadResult result = files.size() == 1
                    ? pdfUploadService.upload(files.getFirst())
                    : pdfUploadService.uploadBundle(files);
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

    @GetMapping(value = "/pdf-bundles/{bundleId}", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> listBundle(@PathVariable String bundleId) {
        List<SavedPdfFile> files = pdfUploadService.listBundle(bundleId);
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StringBuilder page = new StringBuilder("""
                <!doctype html><html lang="ko"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>PDF 목록 · 프린트 링크</title>
                <link rel="stylesheet" href="/print/styles.css"></head>
                <body><main><h1>전송받은 PDF</h1><p>열 파일을 선택하세요.</p><ul>
                """);
        for (SavedPdfFile file : files) {
            page.append("<li><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"/pdf-bundles/")
                    .append(bundleId).append("/files/").append(file.storedFileName()).append("\">")
                    .append(HtmlUtils.htmlEscape(file.originalFileName())).append("</a></li>");
        }
        page.append("</ul></main></body></html>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(page.toString());
    }

    @GetMapping("/pdf-bundles/{bundleId}/files/{fileName}")
    public ResponseEntity<Resource> getBundleFile(@PathVariable String bundleId,
                                                  @PathVariable String fileName) {
        return pdfResponse(pdfUploadService.getBundleFile(bundleId, fileName));
    }

    @DeleteMapping("/pdf-bundles/{bundleId}")
    public ResponseEntity<Void> cancelBundle(@PathVariable String bundleId,
            @RequestHeader("X-Cancel-Token") String token) throws IOException {
        return pdfUploadService.cancelBundle(bundleId, token)
                ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
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

        return pdfResponse(pdfUploadService.getPdf(fileName));
    }

    private ResponseEntity<Resource> pdfResponse(Resource resource) {

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