package com.saveetha.flownotify.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Map

interface ApiService {
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v1/tasks/")
    fun createTask(@Body request: CreateTaskRequest): Call<TaskResponse>

    @GET("api/tasks") // Or your actual endpoint
    fun getTasks(@Query("filter") filter: String): Call<Map<String, List<TaskResponse>>>

    @POST("api/events/")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<Unit> // Assuming a simple success/fail response

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>

}