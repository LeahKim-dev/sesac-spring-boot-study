package com.example.personal_project.presentation;

import com.example.personal_project.application.ItemService;
import com.example.personal_project.domain.Item;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 상품 id로 조회
    @GetMapping(value = "/items/{id}")
    public ItemDto findById(@PathVariable Long id){
        return itemService.findById(id);
    }

    // 상품 전체 조회
    @GetMapping(value = "/items")
    public List<ItemDto> findAll() {
        return itemService.findAll();
    }
}
