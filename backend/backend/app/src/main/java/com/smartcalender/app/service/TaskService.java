package com.smartcalender.app.service;

import com.smartcalender.app.dto.ActivityDTO;
import com.smartcalender.app.dto.ConvertTaskRequest;
import com.smartcalender.app.dto.CreateTaskRequest;
import com.smartcalender.app.dto.TaskDTO;
import com.smartcalender.app.entity.Activity;
import com.smartcalender.app.entity.Category;
import com.smartcalender.app.entity.Task;
import com.smartcalender.app.entity.User;
import com.smartcalender.app.exception.InvalidDateException;
import com.smartcalender.app.exception.InvalidTimeRangeException;
import com.smartcalender.app.exception.NotFoundException;
import com.smartcalender.app.repository.ActivityRepository;
import com.smartcalender.app.repository.CategoryRepository;
import com.smartcalender.app.repository.TaskRepository;
import com.smartcalender.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing tasks assigned to a user.
 * Provides functionalities to create, edit, delete, fetch, and manage tasks.
 * Is called upon from the TaskController and divides the work between the repository and the mapper.
 * @author David Lexe, Carl Lundholm, Isaac Löwenthaal Carter
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;


    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ActivityRepository activityRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new task based on the provided TaskDTO and associates it with the logged-in user.
     * Validates the category if provided and ensures it belongs to the user.
     *
     * @param newTask the data transfer object containing the details of the task to be created
     * @param currentUser the user details of the currently logged-in user
     * @return a data transfer object representing the created task
     * @throws IllegalArgumentException if the specified category ID does not exist or does not belong to the user
     * @author Carl Lundholm, David Lexe
     */
    @Transactional
    public TaskDTO createTask(CreateTaskRequest newTask, UserDetails currentUser) {
        User user = getUser(currentUser);

        Task task = new Task();
        task.setName(newTask.getName());
        task.setDescription(newTask.getDescription());
        task.setDate(newTask.getDate());
        task.setLocation(newTask.getLocation());
        task.setCompleted(newTask.isCompleted());
        task.setUser(user);
        task.setRecurrence(newTask.getRecurrence());

        if (newTask.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUser(newTask.getCategoryId(), user)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            task.setCategory(category);
        }

        Task savedTask = taskRepository.save(task);
        return new TaskDTO(savedTask);
    }

    /**
     * Edits an existing task associated with the given ID and updates its details based on the provided information.
     * Validates the category if specified and ensures it belongs to the logged-in user.
     *
     * @param id the ID of the task to be edited
     * @param newTask the data transfer object containing the updated details of the task
     * @param currentUser the user details of the currently logged-in user
     * @return a data transfer object representing the updated task
     * @throws IllegalArgumentException if the specified category ID does not exist or does not belong to the user
     * @author Carl Lundholm, David Lexe
     */
    @Transactional
    public TaskDTO editTask(Long id, TaskDTO newTask, UserDetails currentUser) {
        User user = getUser(currentUser);
        Task taskToEdit = getTask(id, user);

        taskToEdit.setName(newTask.getName());
        taskToEdit.setDescription(newTask.getDescription());
        taskToEdit.setDate(newTask.getDate());
        taskToEdit.setLocation(newTask.getLocation());
        taskToEdit.setCompleted(newTask.isCompleted());
        if (newTask.getRecurrence() != null) {
            taskToEdit.setRecurrence(newTask.getRecurrence());
        }

        if (newTask.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUser(newTask.getCategoryId(), user)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            taskToEdit.setCategory(category);
        }

        Task updatedTask = taskRepository.save(taskToEdit);
        return new TaskDTO(updatedTask);
    }

    /**
     * Deletes a task identified by its ID that belongs to the current user.
     * If the task cannot be found or does not belong to the user, an exception is thrown.
     *
     * @param id the ID of the task to delete
     * @param currentUser the user details of the currently logged-in user
     * @throws RuntimeException if no task with the specified ID exists for the user
     * @author Carl Lundholm, David Lexe
     */
    @Transactional
    public ResponseEntity<Boolean> deleteTask(Long id, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Optional<Task> taskOptional = taskRepository.findByIdAndUser(id, user);
        if (taskOptional.isPresent()) {
            taskRepository.delete(taskOptional.get());
            return ResponseEntity.status(HttpStatus.OK).body(true);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    /**
     * Toggles the completion status of a task identified by its ID for the specified user.
     * Retrieves the task associated with the provided ID and the current user,
     * updates its completion status, and saves the updated task to the repository.
     *
     * @param id the ID of the task whose completion status is to be toggled
     * @param currentUser the user details of the currently logged-in user
     * @return a data transfer object representing the updated task with the toggled completion status
     * @throws RuntimeException if the task with the specified ID cannot be found for the user
     * @author Carl Lundholm, David Lexe
     */
    public TaskDTO toggleTaskCompletion(Long id, UserDetails currentUser) {
        User user = getUser(currentUser);
        Task taskToEdit = getTask(id, user);

        taskToEdit.toggleCompleted();
        taskRepository.save(taskToEdit);
        return new TaskDTO(taskToEdit);
    }

    /**
     * Converts a task into an activity based on the provided request data and user details.
     * The method validates the task's existence, checks the provided date and time range,
     * and maps the task to a corresponding activity.
     *
     * @param id the unique identifier of the task to be converted
     * @param taskRequest the request object containing details required for the conversion,
     *                    including date, start time, and end time
     * @param currentUser the details of the currently authenticated user performing the conversion
     * @return an ActivityDTO object representing the newly created activity
     * @throws IllegalArgumentException if the start time or end time is not provided,
     *                                  if the start time is after the end time,
     *                                  or if the date is missing and cannot be derived from the task
     * @author David Lexe, Carl Lundholm
     */
    @Transactional
    public ActivityDTO convertTaskToActivity(Long id, ConvertTaskRequest taskRequest, UserDetails currentUser) {
        User user = getUser(currentUser);
        Task task = getTask(id, user);

        if (taskRequest.getStartTime() == null || taskRequest.getEndTime() == null) {
            throw new InvalidTimeRangeException("Start time and end time must be provided");
        }
        if (taskRequest.getStartTime().isAfter(taskRequest.getEndTime())) {
            throw new InvalidTimeRangeException("Start time must be before or equal to end time");
        }
        if (taskRequest.getDate() == null) {
            if (task.getDate() == null) {
                throw new InvalidDateException("Date must be provided");
            } else {
                taskRequest.setDate(task.getDate());
            }
        }

        Activity activity = new Activity();
        activity.setName(task.getName());
        activity.setDescription(task.getDescription());
        activity.setLocation(task.getLocation());
        activity.setDate(taskRequest.getDate());
        activity.setStartTime(taskRequest.getStartTime());
        activity.setEndTime(taskRequest.getEndTime());
        activity.setUser(user);
        activity.setRecurrence(task.getRecurrence());

        if (task.getCategory() != null) {
            Category category = categoryRepository.findByIdAndUser(task.getCategory().getId(), user)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            activity.setCategory(category);
        }
        taskRepository.delete(task);
        activityRepository.save(activity);
        return new ActivityDTO(activity, null);
    }

    /**
     * Retrieves a list of tasks associated with the given user.
     *
     * @param currentUser the details of the current user for whom tasks are being retrieved
     * @return a list of TaskDTO objects representing the tasks assigned to the user
     * @author David Lexe, Carl Lundholm
     */
    public List<TaskDTO> getTasksForUser(UserDetails currentUser) {
        User user = getUser(currentUser);

        return taskRepository.findByUser(user).stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a user's specific task by its identifier.
     *
     * @param id the unique identifier of the task to retrieve
     * @param currentUser the details of the user making the request
     * @return the TaskDTO object containing the details of the task
     * @author David Lexe, Carl Lundholm
     */
    public TaskDTO getUserTaskById(Long id, UserDetails currentUser) {
        User user = getUser(currentUser);
        Task task = getTask(id, user);

        return new TaskDTO(task);
    }

    /**
     * Retrieves a list of tasks associated with a specific category for the given user.
     *
     * @param categoryId the ID of the category whose tasks are to be retrieved
     * @param currentUser the details of the currently authenticated user
     * @return a list of TaskDTO objects representing the tasks for the specified category
     * @throws RuntimeException if the category is not found for the given user
     * @author Carl Lundholm, David Lexe
     */
    public List<TaskDTO> getTasksByCategory(long categoryId, UserDetails currentUser) {
        User user = getUser(currentUser);

        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        List<Task> tasks = taskRepository.findByUserAndCategory(user, category);
        return tasks.stream().map(TaskDTO::new).collect(Collectors.toList());
    }

    /**
     * Retrieves a task associated with the given ID and the current user.
     *
     * @param id the unique identifier of the task to be retrieved
     * @param currentUser the user attempting to retrieve the task
     * @return the Task object associated with the given ID and user
     * @throws RuntimeException if no task is found for the given ID and user
     * @author Carl Lundholm, David Lexe, Isaac Löwenthaal Carter
     */
    private Task getTask(Long id, User currentUser) {
        return taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    /**
     * Retrieves a User entity based on the provided UserDetails object.
     *
     * @param currentUser the UserDetails object containing the required information to retrieve the user
     * @return the User entity associated with the provided username
     * @throws NotFoundException if no user is found with the specified username
     * @author Carl Lundholm, David Lexe, Isaac Löwenthaal Carter
     */
    private User getUser(UserDetails currentUser) {
        return userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
