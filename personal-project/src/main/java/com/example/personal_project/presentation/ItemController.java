package com.example.personal_project.presentation;

import com.example.personal_project.application.ItemService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 상품 등록
    @PostMapping(value = "/items")
    public ItemDto add(
            @RequestBody ItemDto itemDto) {
        return itemService.add(itemDto);
    }
}
