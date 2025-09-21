package com.saveetha.flownotify.network

import com.google.gson.annotations.SerializedName

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
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
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

// Data classes for Events

data class CreateEventRequest(
    val title: String,
    val location: String?,
    val date: String, // "YYYY-MM-DD"
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val category: String,
    val notes: String?,
    val reminder: Int // in minutes before
)

// Data classes for Dashboard Summary Response

data class DashboardSummaryResponse(
    val todaysFlow: TodaysFlow,
    val upcomingTasks: List<UpcomingTask>,
    val todaysSchedule: List<ScheduleEvent>
)

data class TodaysFlow(
    val percentage: Int
)

data class UpcomingTask(
    val id: String,
    val title: String,
    val time: String,
    val priority: String,
    val dueDate: String,
    val description: String?
)

data class ScheduleEvent(
    val id: String,
    val title: String,
    val time: String,
    val location: String
)

data class Event(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val category: String
)

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: String,
    val time: String,
    val priority: String,
    val isCompleted: Boolean
)
