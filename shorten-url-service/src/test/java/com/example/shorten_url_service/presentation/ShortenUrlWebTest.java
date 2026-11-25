package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.application.SimpleShortenUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortenUrlRestController.class)
class ShortenUrlWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean SimpleShortenUrlService service;

    @Test
    void accepts2048CharactersAndRejects2049() throws Exception {
        String prefix = "https://example.com/";
        String url = prefix + "a".repeat(2048 - prefix.length());
        submit(url).andExpect(status().isOk());
        submit(url + "a").andExpect(status().isBadRequest());
        verify(service, times(1)).generateShortenUrl(any());
    }

    @Test
    void checksKoreanUrlAfterEncoding() throws Exception {
        String url = "https://example.com/" + "한".repeat(225);
        submit(url).andExpect(status().isOk());
        submit(url + "한").andExpect(status().isBadRequest());
        verify(service, times(1)).generateShortenUrl(any());
    }

    @Test
    void ignoresPathsOutsideTheCodeFormat() throws Exception {
        for (String path : new String[]{"/favicon.ico", "/1234567", "/shortenUrl/123456789"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }
        verifyNoInteractions(service);
    }

    @Test
    void redirectsWithLocationAndNoStore() throws Exception {
        String url = "https://example.com/";
        when(service.getOriginalUrlByShortenUrlKey("01234567")).thenReturn(url);
        mvc.perform(get("/01234567"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", url))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    private org.springframework.test.web.servlet.ResultActions submit(String url) throws Exception {
        return mvc.perform(post("/shortenUrl").contentType("application/json")
                .content("{\"originalUrl\":\"" + url + "\"}"));
    }
}
