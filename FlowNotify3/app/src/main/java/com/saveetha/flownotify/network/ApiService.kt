package com.saveetha.flownotify.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/tasks")
    fun createTask(@Body request: CreateTaskRequest): Call<TaskResponse>

    @GET("api/tasks")
    suspend fun getTasks(
        @Query("filter") filter: String,
        @Query("search") search: String?
    ): Response<Map<String, List<Task>>>

    @POST("api/events")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<Unit>

    @GET("api/events")
    suspend fun getEvents(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<Event>>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>
}
