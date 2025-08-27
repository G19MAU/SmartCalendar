package se.umu.calu0217.smartcalendar.domain

import java.time.LocalDateTime

data class LoginRequest(
    val email: String,
    val password: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

data class ChangeEmailRequest(
    val newEmail: String,
    val password: String
)

data class DeleteAccountRequest(
    val password: String
)

data class CreateActivityRequest(
    val title: String,
    val description: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val categoryId: Int,
    val recurrence: Recurrence = Recurrence.NONE
)

data class ActivityDTO(
    val id: Int,
    val title: String,
    val description: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val category: String?,
    val updatedAt: String?,
    val recurrence: Recurrence = Recurrence.NONE
)

data class CreateCategoryRequest(
    val name: String,
    val color: String?
)

data class CategoryDTO(
    val id: Int,
    val name: String,
    val color: String?
)

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val dueDate: LocalDateTime,
    val categoryId: Int,
    val recurrence: Recurrence = Recurrence.NONE
)

data class TaskDTO(
    val id: Int,
    val title: String,
    val description: String?,
    val dueDate: LocalDateTime,
    val completed: Boolean,
    val category: String?,
    val updatedAt: String?,
    val recurrence: Recurrence = Recurrence.NONE
)

data class ConvertTaskRequest(
    val startDate: String,
    val endDate: String
)

data class UserDTO(
    val id: Int,
    val email: String,
    val fullName: String,
    val profileIcon: String?
)

data class TaskStatsDTO(
    val total: Int,
    val completed: Int,
    val pending: Int
)

data class ActivityStatsDTO(
    val total: Int,
    val upcoming: Int,
    val ongoing: Int
)

data class ResponseDTO(
    val message: String?,
    val email: String?
)

data class LoginResponseDTO(
    val accessToken: String,
    val refreshToken: String
)
