package com.example.sesac_spring_boot_study;

public class StudyMember {
    public enum Status {
        CONFIRMED,
        WAITING
    }
    private Long id;
    private String name;
    private Status status;

    public StudyMember(Long id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
}
