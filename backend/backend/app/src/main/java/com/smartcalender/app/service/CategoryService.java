package com.smartcalender.app.service;

import com.smartcalender.app.dto.CategoryDTO;
import com.smartcalender.app.dto.CreateCategoryRequest;
import com.smartcalender.app.entity.Category;
import com.smartcalender.app.entity.User;
import com.smartcalender.app.exception.AlreadyExistsException;
import com.smartcalender.app.exception.NotFoundException;
import com.smartcalender.app.repository.CategoryRepository;
import com.smartcalender.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A service class responsible for handling operations related to categories,
 * such as creating, editing, deleting, and retrieving categories.
 * This class includes transactional methods for managing category data in the database.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new category based on the provided data and associates it with the current user.
     * It verifies if a category with the same name already exists for the user.
     * If a duplicate category is found, it throws an IllegalArgumentException.
     *
     * @param categoryRequest the request containing the details of the category to be created, including name and color
     * @param currentUser the authenticated user details used to associate the category with the user
     * @return a {@code CategoryDTO} representing the newly created category
     * @throws IllegalArgumentException if a category with the same name already exists
     */
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest categoryRequest, UserDetails currentUser) {
        User user = getUser(currentUser);


        if (categoryRepository.findByNameAndUser(categoryRequest.getName(), user).isPresent()) {
            throw new AlreadyExistsException("Category already exists");
        } else {
            Category category = new Category();
            category.setName(categoryRequest.getName());
            category.setColor(categoryRequest.getColor());
            category.setUser(user);

            categoryRepository.save(category);
            return new CategoryDTO(category);
        }
    }

    /**
     * Deletes a category associated with the current user, identified by its unique ID.
     * This method ensures the authenticated user has ownership of the category before deletion.
     *
     * @param id the unique identifier of the category to delete
     * @param currentUser the authenticated user details used to verify category ownership
     * @throws IllegalArgumentException if the category does not exist or does not belong to the user
     */
    @Transactional
    public void deleteCategory(Long id, UserDetails currentUser) {
        User user = getUser(currentUser);

        Category category = getCategory(id, user);

        categoryRepository.delete(category);
    }

    /**
     * Edits an existing category associated with the current user, identified by its unique ID.
     * Updates are based on the provided {@code CategoryDTO}. Only non-null fields in the updated
     * category will overwrite the existing category's fields.
     *
     * @param id the unique identifier of the category to edit
     * @param UpdatedCategory the updated category data encapsulated in a {@code CategoryDTO}
     * @param currentUser the authenticated user details used to verify category ownership
     * @return a {@code CategoryDTO} representing the updated category
     * @throws IllegalArgumentException if the category does not exist or does not belong to the user
     */
    @Transactional
    public CategoryDTO editCategory(Long id, CategoryDTO UpdatedCategory, UserDetails currentUser) {
        User user = getUser(currentUser);

        Category category = getCategory(id, user);

        category.setName(UpdatedCategory.getName() != null ? UpdatedCategory.getName() : category.getName());
        category.setColor(UpdatedCategory.getColor() != null ? UpdatedCategory.getColor() : category.getColor());
        categoryRepository.save(category);
        return new CategoryDTO(category);
    }

    /**
     * Retrieves all categories associated with the authenticated user.
     * This method fetches the categories from the repository, transforms them into {@code CategoryDTO} objects,
     * and returns them as a list.
     *
     * @param currentUser the authenticated user details used to retrieve the associated categories
     * @return a list of {@code CategoryDTO} objects representing the user's categories
     */
    public List<CategoryDTO> getAllCategories(UserDetails currentUser) {
        User user = getUser(currentUser);

        return categoryRepository.findByUser(user).stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a category by its unique ID associated with the authenticated user.
     * This method ensures the user has access to the requested category.
     *
     * @param categoryId the unique identifier of the category to retrieve
     * @param currentUser the authenticated user details used to verify category ownership
     * @return a {@code CategoryDTO} representing the category associated with the provided ID
     * @throws IllegalArgumentException if the category does not exist or does not belong to the user
     */
    public CategoryDTO getCategoryById(Long categoryId, UserDetails currentUser) {
        User user = getUser(currentUser);

        return new CategoryDTO(getCategory(categoryId, user));
    }

    /**
     * Retrieves a category by its unique ID associated with the specified user.
     * Ensures that the category exists and belongs to the provided user.
     *
     * @param id the unique identifier of the category to retrieve
     * @param currentUser the user the category is associated with
     * @return the {@code Category} entity if found and associated with the user
     * @throws IllegalArgumentException if the category does not exist or is not associated with the user
     */
    private Category getCategory(Long id, User currentUser) {
        return categoryRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    /**
     * Retrieves the {@code User} entity associated with the provided {@code UserDetails}.
     * This method fetches the user based on the username from the {@code UserDetails}.
     * If the user is not found, a {@code NotFoundException} is thrown.
     *
     * @param currentUser the authenticated user details containing the username
     * @return the {@code User} entity associated with the provided {@code UserDetails}
     * @throws NotFoundException if the user is not found in the repository
     */
    private User getUser(UserDetails currentUser) {
        return userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

}
