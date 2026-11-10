package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.domain.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryItemRepository implements ItemRepository {
    private List<Item> items = new CopyOnWriteArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Item add(Item item) {
        Item savedItem = new Item(
                sequence.getAndAdd(1L),
                item.getName(),
                item.getPrice()
        );
        items.add(savedItem);
        return savedItem;
    }

    @Override
    public Item findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("item을 찾지 못했습니다."));
    }

    @Override
    public List<Item> findAll() {
        List<Item> itemList = new ArrayList<>(items);
        return itemList;
    }

    @Override
    public List<Item> findByNameContaining(String name) {
        return items.stream()
                .filter(item -> item.getName().contains(name))
                .toList();
    }
}
