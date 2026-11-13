package com.example.personal_project.presentation;

import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.infrastructure.DummyJsonProduct;
import com.example.personal_project.infrastructure.DummyJsonProductList;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/external-products")
public class ExternalProductController {
    private final DummyJsonClient client;

    public ExternalProductController(DummyJsonClient client) {
        this.client = client;
    }

    // 상품 조회
    @GetMapping("/{id}")
    public DummyJsonProduct findById(
            @PathVariable("id") long id) {
        return client.findById(id);
    }

    // 목록 조회
    @GetMapping
    public DummyJsonProductList findAll (
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return client.findAll(limit, skip);
    }


}
