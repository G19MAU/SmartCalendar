package com.smartcalender.app.service;

import com.smartcalender.app.dto.ActivityDTO;
import com.smartcalender.app.dto.CreateActivityRequest;
import com.smartcalender.app.entity.Activity;
import com.smartcalender.app.repository.ActivityRepository;
import com.smartcalender.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    public void testCreateActivity() {
        UserDetails currentUser = new User("username", "password",
                new ArrayList<>());

        CreateActivityRequest createRequest = new CreateActivityRequest();
        createRequest.setName("Testing Activity");
        createRequest.setDate(LocalDate.now());
        createRequest.setStartTime(LocalTime.now());
        createRequest.setEndTime(LocalTime.now());
        createRequest.setLocation("Test Location");

        when(userRepository.findByUsername(any()))
                .thenReturn(Optional.of(new com.smartcalender.app.entity.User()));

        when(activityRepository.findByUser(any()))
                .thenReturn(new ArrayList<>());

        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        ActivityDTO activity = activityService.createActivity(createRequest, currentUser).getBody();

        assertNotNull(activity);
        assertEquals(createRequest.getName(), activity.getName());

        // Verify interactions
        verify(userRepository, times(1)).findByUsername(currentUser.getUsername());
        verify(activityRepository, times(1)).save(any(Activity.class));
        verify(activityRepository, times(1)).findByUser(any());
    }

    @Test
    public void testCreateActivityWithOverlap() {
        UserDetails currentUser = new User("username", "password",
                new ArrayList<>());

        CreateActivityRequest createRequest = new CreateActivityRequest();
        createRequest.setName("Testing Activity");
        createRequest.setDate(LocalDate.now());
        createRequest.setStartTime(LocalTime.of(10, 0));
        createRequest.setEndTime(LocalTime.of(11, 0));
        createRequest.setLocation("Test Location");

        com.smartcalender.app.entity.User mockUser = new com.smartcalender.app.entity.User();
        when(userRepository.findByUsername(any()))
                .thenReturn(Optional.of(mockUser));

        Activity existingActivity = new Activity();
        existingActivity.setName("Existing Activity");
        existingActivity.setDate(LocalDate.now());
        existingActivity.setStartTime(LocalTime.of(10, 30));
        existingActivity.setEndTime(LocalTime.of(11, 30));
        existingActivity.setUser(mockUser);

        when(activityRepository.findByUser(any()))
                .thenReturn(List.of(existingActivity));

        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(i -> {
                    Activity a = (Activity) i.getArguments()[0];
                    Activity savedActivity = new Activity();
                    savedActivity.setName(a.getName());
                    savedActivity.setDate(a.getDate());
                    savedActivity.setStartTime(a.getStartTime());
                    savedActivity.setEndTime(a.getEndTime());
                    savedActivity.setUser(a.getUser());
                    return savedActivity;
                });

        ActivityDTO activity = activityService.createActivity(createRequest, currentUser).getBody();

        assertNotNull(activity);
        assertEquals(createRequest.getName(), activity.getName());
        // Activity should have warnings about overlap
        assertNotNull(activity.getWarnings());
        assertFalse(activity.getWarnings().isEmpty());

        verify(userRepository, times(1)).findByUsername(currentUser.getUsername());
        verify(activityRepository, times(1)).save(any(Activity.class));
        verify(activityRepository, times(1)).findByUser(any());
    }
}