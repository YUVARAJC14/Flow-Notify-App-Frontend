package com.saveetha.flownotify.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Optional

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: User
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

data class Event(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val category: String,
    val notes: String?
) : Serializable

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
    @SerializedName("id")
    val id: String,
    val title: String,
    val time: String,
    val priority: String,
    val dueDate: String,
    val description: String?,
    val completed: Boolean
)

data class ScheduleEvent(
    val id: String,
    val title: String,
    val time: String,
    val endTime: String,
    val location: String,
    val category: String,
    val notes: String?,
    val date: String
)

data class EventUpdateRequest(
    @SerializedName("isCompleted")
    val completed: Boolean
)

data class Task(
    val id: String,
    val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("due_time") val time: String?,
    val priority: String,
    @SerializedName("completed") val isCompleted: Boolean
)

// Data classes for Insights

data class InsightsResponse(
    val flowScore: FlowScore,
    val taskCompletion: List<TaskCompletion>,
    val productiveTimes: List<ProductiveTime>
)

data class FlowScore(
    val score: Int,
    val comparison: Comparison
)

data class Comparison(
    val change: Int,
    val period: String
)

data class TaskCompletion(
    val label: String,
    val completed: Int,
    val total: Int
)

data class ProductiveTime(
    val day: Int,
    val hour: Int,
    val intensity: Float
)
