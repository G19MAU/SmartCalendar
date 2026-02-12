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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
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

        Activity existingActivity = mock(Activity.class);
        when(existingActivity.getId()).thenReturn(1L);
        when(existingActivity.getName()).thenReturn("Existing Activity");
        when(existingActivity.getDate()).thenReturn(LocalDate.now());
        when(existingActivity.getStartTime()).thenReturn(LocalTime.of(10, 30));
        when(existingActivity.getEndTime()).thenReturn(LocalTime.of(11, 30));
        when(existingActivity.getUser()).thenReturn(mockUser);

        when(activityRepository.findByUser(any()))
                .thenReturn(List.of(existingActivity));

        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(i -> {
                    Activity a = (Activity) i.getArguments()[0];
                    Activity savedActivity = mock(Activity.class);
                    when(savedActivity.getId()).thenReturn(2L);
                    when(savedActivity.getName()).thenReturn(a.getName());
                    when(savedActivity.getDate()).thenReturn(a.getDate());
                    when(savedActivity.getStartTime()).thenReturn(a.getStartTime());
                    when(savedActivity.getEndTime()).thenReturn(a.getEndTime());
                    when(savedActivity.getUser()).thenReturn(a.getUser());
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