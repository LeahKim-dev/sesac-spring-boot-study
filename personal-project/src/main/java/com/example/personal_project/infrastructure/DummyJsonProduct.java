package com.example.personal_project.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true) // DTO에 없는 JSON 필드를 무시
public record DummyJsonProduct( // 외부 사이트에서 받은 상품 정보를 담는 그릇
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock) {

}
