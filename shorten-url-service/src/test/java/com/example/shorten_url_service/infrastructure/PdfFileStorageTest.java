package com.example.shorten_url_service.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PdfFileStorageTest {

    @TempDir
    Path directory;

    @Test
    void storesAndLoadsPdf() throws Exception {
        PdfFileStorage storage = new PdfFileStorage(directory);
        String fileName = storage.save(pdf());

        assertTrue(fileName.matches("pdf-[0-9]+\\.pdf"));
        assertArrayEquals(pdf().getBytes(), storage.load(fileName).getContentAsByteArray());
        assertNull(storage.load("../outside.pdf"));
    }

    @Test
    void rejectsEmptyAndInvalidFiles() throws Exception {
        PdfFileStorage storage = new PdfFileStorage(directory);
        assertThrows(IllegalArgumentException.class,
                () -> storage.save(new MockMultipartFile("file", new byte[0])));
        assertThrows(IllegalArgumentException.class,
                () -> storage.save(new MockMultipartFile("file", "test.pdf",
                        "application/pdf", "not a PDF".getBytes(StandardCharsets.US_ASCII))));
        try (var files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void rejectsFilesOverTenMegabytes() {
        PdfFileStorage storage = new PdfFileStorage(directory);
        byte[] oversized = new byte[10 * 1024 * 1024 + 1];
        byte[] header = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(header, 0, oversized, 0, header.length);

        assertThrows(IllegalArgumentException.class,
                () -> storage.save(new MockMultipartFile("file", "large.pdf",
                        "application/pdf", oversized)));
    }

    @Test
    void expiredPdfCannotBeLoadedAndCleanupKeepsOtherFiles() throws Exception {
        PdfFileStorage storage = new PdfFileStorage(directory);
        String expiredName = storage.save(pdf());
        String freshName = storage.save(pdf());
        Path expiredFile = directory.resolve(expiredName);
        Path freshFile = directory.resolve(freshName);
        Path unrelatedFile = Files.writeString(directory.resolve("unrelated.pdf"), "keep");
        Path abandonedUpload = Files.writeString(directory.resolve("pdf-123.part"), "partial");
        FileTime oldTime = FileTime.from(Instant.now().minus(Duration.ofMinutes(11)));

        Files.setLastModifiedTime(expiredFile, oldTime);
        Files.setLastModifiedTime(unrelatedFile, oldTime);
        Files.setLastModifiedTime(abandonedUpload, oldTime);

        assertNull(storage.load(expiredName));
        assertNotNull(storage.load(freshName));
        storage.deleteExpiredFiles();

        assertFalse(Files.exists(expiredFile));
        assertFalse(Files.exists(abandonedUpload));
        assertTrue(Files.exists(freshFile));
        assertTrue(Files.exists(unrelatedFile));
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("file", "test.pdf", "application/pdf",
                "%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII));
    }
}
