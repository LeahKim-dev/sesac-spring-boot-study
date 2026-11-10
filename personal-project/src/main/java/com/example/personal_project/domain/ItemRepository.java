package com.example.personal_project.domain;

import java.util.List;

public interface ItemRepository {
    Item add(Item item);
    List<Item> findAll();
    Item findById(Long id);
}
