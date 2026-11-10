package com.example.personal_project.domain;

public class CollectionItem {

    private Long id;
    private String name;
    private int price;

    public CollectionItem(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
