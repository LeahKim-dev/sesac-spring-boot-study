package com.example.personal_project.presentation;

import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.infrastructure.DummyJsonProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-products")
public class ExternalProductController {
    private final DummyJsonClient client;

    public ExternalProductController(DummyJsonClient client) {
        this.client = client;
    }

    // 조회
    @GetMapping("/{id}")
    public DummyJsonProduct findById(@PathVariable("id") long id) {
        return client.findById(id);
    }
}
