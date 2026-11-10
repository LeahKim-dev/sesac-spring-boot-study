package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.CollectionItem;
import com.example.personal_project.domain.CollectionItemRepository;
import com.example.personal_project.domain.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryCollectionItemRepository implements CollectionItemRepository {
    private List<CollectionItem> items = new CopyOnWriteArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        CollectionItem savedItem = new CollectionItem(
                sequence.getAndAdd(1L),
                collectionItem.getName(),
                collectionItem.getPrice()
        );
        items.add(savedItem);
        return savedItem;
    }

    @Override
    public List<CollectionItem> findAll() {
        List<CollectionItem> itemList = new ArrayList<>(items);
        return itemList;
    }

    @Override
    public CollectionItem findById(Long id) {
        return items.stream()
                .filter(collectionItem -> collectionItem.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("item을 찾지 못했습니다."));
    }
}
