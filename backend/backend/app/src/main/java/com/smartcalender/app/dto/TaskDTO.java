package com.smartcalender.app.dto;

import com.smartcalender.app.entity.Task;
import com.smartcalender.app.entity.Recurrence;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class TaskDTO {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private LocalDate date;
    private String location;
    private boolean completed;
    private Long categoryId;
    private Recurrence recurrence;

    public TaskDTO() {}

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.date = task.getDate();
        this.location = task.getLocation();
        this.completed = task.isCompleted();
        this.categoryId = task.getCategory() != null ? task.getCategory().getId() : null;
        this.recurrence = task.getRecurrence();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }
}
