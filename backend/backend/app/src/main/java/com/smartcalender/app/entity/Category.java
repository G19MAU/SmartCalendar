package com.smartcalender.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_user", columnList = "user_id"),
    @Index(name = "idx_category_user_name", columnList = "user_id, name")
})
public class Category {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Category() {
    }

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
