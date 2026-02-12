package com.smartcalender.app.service;

import com.smartcalender.app.dto.*;
import com.smartcalender.app.entity.Activity;
import com.smartcalender.app.entity.Task;
import com.smartcalender.app.entity.User;
import com.smartcalender.app.repository.ActivityRepository;
import com.smartcalender.app.repository.TaskRepository;
import com.smartcalender.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ActivityRepository activityRepository;

    @Value("${email-verification.required}")
    private boolean emailVerificationRequired;

    public UserService(UserRepository userRepository, TaskRepository taskRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.activityRepository = activityRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        builder.password(user.getPassword());
        builder.authorities("USER");

        return builder.build();
    }

  public TaskStatsDTO getTaskStats(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        List<Task> tasks = taskRepository.findByUser(user);

        int total = tasks.size();
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();
        int active = total - completed;

        return new TaskStatsDTO(total, completed, active);
  }
  
  public ActivityStatsDTO getActivityStats(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        List<Activity> activities = activityRepository.findByUser(user);
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        YearMonth thisMonth = YearMonth.from(today);

        int totalActivitiesToday = (int) activities.stream()
                .filter(activity -> activity.getDate().isEqual(today))
                .count();

        int totalActivitiesThisWeek = (int) activities.stream()
                .filter(activity -> activity.getDate().isAfter(startOfWeek.minusDays(1)))
                .count();

        int totalActivitiesThisMonth = (int) activities.stream()
                .filter(activity -> activity.getDate().getMonth() == thisMonth.getMonth())
                .count();

      long totalMinutesThisWeek = activities.stream()
              .filter(activity -> activity.getDate().isAfter(startOfWeek.minusDays(1)))
              .mapToLong(Activity::getDuration)
              .sum();
      int averageHoursPerWeek = (int) Math.round(totalMinutesThisWeek / 60.0);

      return new ActivityStatsDTO(totalActivitiesToday, totalActivitiesThisWeek, totalActivitiesThisMonth, averageHoursPerWeek);
  }

    public UserDTO getUser(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getId(), user.getUsername(), user.getEmailAddress(), user.getProfileIcon());
    }

    public UserDTO getProfileIcon(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getProfileIcon());
    }

    public UserDTO setProfileIcon(UserDetails currentUser, String profileIcon) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        if (!isValidProfileIcon(profileIcon)) {
            throw new IllegalArgumentException("Invalid profile icon ID");
        }
        user.setProfileIcon(profileIcon);
        userRepository.save(user);
        return new UserDTO(user.getProfileIcon());
    }

    private boolean isValidProfileIcon(String iconId) {
        String[] validIcons = {"icon1", "icon2", "icon3", "icon4", "icon5", "icon6", "icon7", "icon8", "icon9", "icon10"};
        for (String validIcon : validIcons) {
            if (validIcon.equals(iconId)) {
                return true;
            }
        }
        return false;
    }

    public SearchDTO searchForActivitiesOrTasks(UserDetails currentUser, String query) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        List<Task> tasks = taskRepository.findByUserAndNameContainingIgnoreCase(user, query);
        List<Activity> activities = activityRepository.findByUserAndNameContainingIgnoreCase(user, query);

        if (tasks.isEmpty() && activities.isEmpty()) {
            return new SearchDTO(query, new ActivityDTO[0], new TaskDTO[0]);
        }

        return new SearchDTO(
                query,
                activities.stream().map(ActivityDTO::new).toArray(ActivityDTO[]::new),
                tasks.stream().map(TaskDTO::new).toArray(TaskDTO[]::new));}
}
