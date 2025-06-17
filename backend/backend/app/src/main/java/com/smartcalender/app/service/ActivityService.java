package com.smartcalender.app.service;

import com.smartcalender.app.dto.ActivityDTO;
import com.smartcalender.app.dto.CreateActivityRequest;
import com.smartcalender.app.entity.Activity;
import com.smartcalender.app.entity.Category;
import com.smartcalender.app.entity.User;
import com.smartcalender.app.exception.NotFoundException;
import com.smartcalender.app.repository.ActivityRepository;
import com.smartcalender.app.repository.CategoryRepository;
import com.smartcalender.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing activities.
 * Provides methods to create, retrieve, update, and delete activities associated with a user.
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    public ActivityService(ActivityRepository activityRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all activities associated with the currently authenticated user.
     *
     * @param currentUser the details of the currently authenticated user
     * @return a list of {@code ActivityDTO} objects representing the user's activities
     * @throws NotFoundException if the current user cannot be found in the system
     */
    public ResponseEntity<List<ActivityDTO>> getAllActivities(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<ActivityDTO> activities = activityRepository.findByUser(user)
                .stream()
                .map(activity -> new ActivityDTO(activity, checkForOverlaps(activity, user, activity.getId())))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(activities);
    }


    /**
     * Creates a new activity based on the provided request and associates it with the current user.
     * The activity details such as name, description, date, time, location, and optional category
     * are saved and returned in the form of an ActivityDTO.
     *
     * @param request the request object containing the details of the activity to be created
     * @param currentUser the details of the currently authenticated user creating the activity
     * @return an {@code ActivityDTO} object representing the created activity, including any
     * warnings about overlapping activities
     * @throws NotFoundException if the current user cannot be found in the system
     * @throws IllegalArgumentException if the specified category is not found
     */
    @Transactional
    public ResponseEntity<ActivityDTO> createActivity(CreateActivityRequest request, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Activity activity = new Activity();
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setDate(request.getDate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setLocation(request.getLocation());
        activity.setUser(user);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            activity.setCategory(category);
        }

        Activity savedActivity = activityRepository.save(activity);
        List<String> warnings = checkForOverlaps(savedActivity, user, savedActivity.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(new ActivityDTO(savedActivity, warnings));
    }

    /**
     * Deletes an activity associated with the currently authenticated user.
     * The activity is identified by its unique ID and must belong to the user
     * making the request. If the activity is found and deleted successfully,
     * the method returns a positive response, otherwise, a not found response
     * is returned.
     *
     * @param currentUser the details of the currently authenticated user
     * @param id the unique identifier of the activity to be deleted
     * @return a {@code ResponseEntity} containing {@code true} if the activity
     *         was deleted successfully, or {@code false} if the activity was
     *         not found
     * @throws NotFoundException if the current user cannot be found in the system
     */
    @Transactional
    public ResponseEntity<Boolean> deleteActivity(UserDetails currentUser, Long id) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Activity activityToDelete = activityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + id));

            activityRepository.delete(activityToDelete);
            return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    /**
     * Edits an existing activity associated with the currently authenticated user.
     * The activity is identified by its unique ID and is updated with the details
     * provided in the ActivityDTO. The method verifies user authorization, validates
     * category existence if specified, and checks for overlapping activities.
     *
     * @param currentUser the details of the currently authenticated user
     * @param id the unique identifier of the activity to be edited
     * @param activityDTO the data transfer object containing updated activity details
     * @return an Optional containing the updated ActivityDTO, including any warnings
     *         about overlapping activities, if the activity was successfully edited;
     *         otherwise, an empty Optional if the activity is not found or not accessible
     *         by the current user
     * @throws NotFoundException if the current user cannot be found in the system
     * @throws RuntimeException if the specified category is not found
     */
    @Transactional
    public ResponseEntity<ActivityDTO> editActivity(UserDetails currentUser, Long id, ActivityDTO activityDTO) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Activity activityToEdit = activityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + id));

            activityToEdit.setName(activityDTO.getName());
            activityToEdit.setDescription(activityDTO.getDescription());
            activityToEdit.setDate(activityDTO.getDate());
            activityToEdit.setStartTime(activityDTO.getStartTime());
            activityToEdit.setEndTime(activityDTO.getEndTime());
            activityToEdit.setLocation(activityDTO.getLocation());

            if (activityDTO.getCategoryId() != null) {
                Category category = categoryRepository.findById(activityDTO.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("Category not found with ID: " + activityDTO.getCategoryId()));
                activityToEdit.setCategory(category);
            }

            Activity savedActivity = activityRepository.save(activityToEdit);
            List<String> warnings = checkForOverlaps(savedActivity, user, id);

            return ResponseEntity.status(HttpStatus.OK).body(new ActivityDTO(savedActivity, warnings));
    }

    /**
     * Retrieves a specific activity associated with the given ID for the current authenticated user.
     *
     * @param id the unique identifier of the activity to be retrieved
     * @param currentUser the details of the currently authenticated user
     * @return an Optional containing the {@code ActivityDTO} object if the activity is found
     *         and belongs to the current user; an empty Optional otherwise
     * @throws NotFoundException if the current user cannot be found in the system
     */
    public ResponseEntity<ActivityDTO> getActivity(long id, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return activityRepository.findByIdAndUser(id, user)
                .map(activity -> ResponseEntity.status(HttpStatus.OK).body(new ActivityDTO(activity, checkForOverlaps(activity, user, id))))
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + id));
    }

    /**
     * Retrieves a list of ongoing activities for the currently authenticated user.
     *
     * @param currentUser the details of the currently authenticated user
     * @return a list of {@code ActivityDTO} objects representing the user's ongoing activities
     * @throws NotFoundException if the current user cannot be found in the system
     */
    public ResponseEntity<List<ActivityDTO>> getOngoingActivities(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<Activity> ongoingActivities = activityRepository.findOngoingActivities(user.getId(), currentDate, currentTime);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ongoingActivities.stream()
                        .map(activity -> new ActivityDTO(activity, checkForOverlaps(activity, user, activity.getId())))
                        .collect(Collectors.toList()));
    }


    /**
     * Retrieves a list of future activities for the currently authenticated user.
     *
     * @param currentUser the details of the currently authenticated user
     * @return a list of {@code ActivityDTO} objects representing the user's future activities
     * @throws NotFoundException if the current user cannot be found in the system
     */
    public ResponseEntity<List<ActivityDTO>> getFutureActivities(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDate currentDate = LocalDate.now();

        return ResponseEntity.status(HttpStatus.OK)
                .body(activityRepository.findUpcomingActivities(user.getId(), currentDate)
                        .stream()
                        .map(activity -> new ActivityDTO(activity, checkForOverlaps(activity, user, activity.getId())))
                        .collect(Collectors.toList()));
    }

    /**
     * Retrieves a list of activities belonging to a specific category for the currently authenticated user.
     *
     * @param currentUser the details of the currently authenticated user
     * @param categoryId the unique identifier of the category to filter activities by
     * @return a list of {@code ActivityDTO} objects representing the activities in the specified category
     *         for the authenticated user
     * @throws NotFoundException if the current user cannot be found in the system
     */
    public ResponseEntity<List<ActivityDTO>> getActivitiesByCategory(UserDetails currentUser, Long categoryId) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.status(HttpStatus.OK)
                .body(activityRepository.findByCategoryIdAndUserId(categoryId, user.getId())
                        .stream()
                        .map(activity -> new ActivityDTO(activity, checkForOverlaps(activity, user, activity.getId())))
                        .collect(Collectors.toList()));
    }

  /**
   * Retrieves a list of activity DTOs for the given user filtered by the specified date range.
   *
   * @param currentUser the details of the currently authenticated user
   * @param start the start date (inclusive) for filtering activities
   * @param end the end date (inclusive) for filtering activities
   * @return a list of activity DTOs within the specified date range
   * @throws NotFoundException if the current user cannot be found
   */
  public ResponseEntity<List<ActivityDTO>> getActivitiesBetween(UserDetails currentUser, LocalDate start, LocalDate end) {
      User user = userRepository.findByUsername(currentUser.getUsername())
              .orElseThrow(() -> new NotFoundException("User not found"));

      return ResponseEntity.status(HttpStatus.OK)
              .body(activityRepository.findByUserAndDateBetween(user, start, end)
                      .stream()
                      .map(activity -> new ActivityDTO(activity, checkForOverlaps(activity, user, activity.getId())))
                      .collect(Collectors.toList()));
  }

    /**
     * Checks for overlapping activities for a given user and activity, excluding a specific activity ID if provided.
     * If overlaps are detected, warning messages are generated describing the conflicts.
     *
     * @param activity the activity to check for overlaps
     * @param user the user whose activities are to be checked for potential overlaps
     * @param excludeId an optional ID of the activity to exclude from the overlap check
     * @return a list of warning messages indicating overlapping activities, or an empty list if no overlaps are found
     */
    private List<String> checkForOverlaps(Activity activity, User user, Long excludeId) {
        List<Activity> userActivities = activityRepository.findByUser(user);
        List<String> warnings = new ArrayList<>();

        for (Activity existing : userActivities) {
            if (!userActivities.isEmpty() && (!existing.getId().equals(excludeId)) &&
                    activity.getDate().equals(existing.getDate()) &&
                    (activity.getStartTime().isBefore(existing.getEndTime()) && activity.getEndTime().isAfter(existing.getStartTime()))) {
                warnings.add("This activity overlaps with '" + existing.getName() + "' on " + existing.getDate() +
                        " from " + existing.getStartTime() + " to " + existing.getEndTime());
            }
        }
        return warnings;
    }
}
