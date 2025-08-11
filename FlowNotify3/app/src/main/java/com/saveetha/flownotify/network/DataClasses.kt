package com.saveetha.flownotify.network

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: User
)

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val dueDate: String, // "YYYY-MM-DD"
    val dueTime: String, // "HH:mm"
    val priority: String,
    val reminders: List<String>
)

data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: String,
    val dueTime: String,
    val priority: String,
    val reminders: List<String>,
    val isCompleted: Boolean,
    val createdAt: String
)