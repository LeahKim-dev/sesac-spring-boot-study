package com.example.personal_project.application;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.presentation.ItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ValidationService validationService;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       ValidationService validationService) {
        this.itemRepository = itemRepository;
        this.validationService = validationService;
    }

    public List<ItemDto> findAll() {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> itemDtos = items.stream()
                                .map(item -> ItemDto.from(item))
                                .toList();
        return itemDtos;
    }

    public ItemDto findById(Long id) {
        Item item = itemRepository.findById(id);
        return ItemDto.from(item);
    }


    public ItemDto add(ItemDto itemDto) {
        String name = validationService.validateName(itemDto.name());
        int price = validationService.validatePrice(itemDto.price());

        Item item = new Item(null, name, price);
        Item savedItem = itemRepository.add(item);
        return ItemDto.from(savedItem);
    }

    public List<ItemDto> recommend(int budget) {
        int validBudget = validationService.validateBudget(budget);
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> item.getPrice() <= validBudget)
                .sorted(Comparator.comparing(item -> item.getPrice()))
                .limit(3)
                .map(item -> ItemDto.from(item))
                .toList();
    }
}
