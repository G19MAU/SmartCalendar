package com.smartcalender.app.dto;

import com.smartcalender.app.entity.Activity;

import java.time.LocalDate;
import java.time.LocalTime;

public class ActivityDTO {
    private String name;
    private String description;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Long userId;
    private String userName;
    private Long duration;
    private Boolean onGoing;
    private Boolean future;
    private Boolean validTimeRange;


    public ActivityDTO() {
    }

    public ActivityDTO(Activity activity) {
        this.name = activity.getName();
        this.description = activity.getDescription();
        this.location = activity.getLocation();
        this.date = activity.getDate();
        this.startTime = activity.getStartTime();
        this.endTime = activity.getEndTime();
        this.categoryId = activity.getCategory().getId();
        this.userId = activity.getUser().getId();
        this.duration = activity.getDuration();
        this.onGoing = activity.isOnGoing();
        this.future = activity.isFuture();
        this.validTimeRange = activity.isValidTimeRange();
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Boolean getOnGoing() {
        return onGoing;
    }

    public void setOnGoing(Boolean onGoing) {
        this.onGoing = onGoing;
    }

    public Boolean getFuture() {
        return future;
    }

    public void setFuture(Boolean future) {
        this.future = future;
    }

    public Boolean getValidTimeRange() {
        return validTimeRange;
    }

    public void setValidTimeRange(Boolean validTimeRange) {
        this.validTimeRange = validTimeRange;
    }
}