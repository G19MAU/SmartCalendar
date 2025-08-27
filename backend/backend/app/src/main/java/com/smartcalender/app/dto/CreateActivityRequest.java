package com.smartcalender.app.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

import com.smartcalender.app.entity.Recurrence;

public class CreateActivityRequest {
    @NotNull
    @NotBlank
    public String name;

    private String description;
    private String location;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private Long categoryId;
    private Long userId;

    private Recurrence recurrence = Recurrence.NONE;

    @AssertTrue(message = "Start time must be before end time")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return false; // Let @NotNull handle null validation
        }
        return startTime.isBefore(endTime);
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public @NotNull LocalDate getDate() {
        return date;
    }

    public void setDate(@NotNull LocalDate date) {
        this.date = date;
    }

    public @NotNull LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@NotNull LocalTime startTime) {
        this.startTime = startTime;
    }

    public @NotNull LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(@NotNull LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }
}