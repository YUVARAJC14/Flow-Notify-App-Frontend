package com.saveetha.flownotify.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("api/auth/login")
    fun login(@Field("username") username: String, @Field("password") password: String): Call<LoginResponse>

    @POST("api/tasks")
    fun createTask(@Body request: CreateTaskRequest): Call<TaskResponse>

    @GET("api/tasks")
    suspend fun getTasks(
        @Query("filter") filter: String,
        @Query("search") search: String?
    ): Response<Map<String, List<Task>>>

    @POST("api/events")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<Event>

    @GET("api/events")
    suspend fun getEvents(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<Event>>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>

    @GET("api/insights")
    suspend fun getInsights(@Query("period") period: String): Response<InsightsResponse>

    @GET("api/users/me")
    suspend fun getUserProfile(): Response<User>

    @PATCH("api/users/me/settings")
    suspend fun updateUserSettings(@Body body: Map<String, String>): Response<Unit>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: Map<String, String>): Response<Unit>

    @GET("api/insights/activity-summary")
    suspend fun getActivitySummary(@Query("period") period: String): Response<ActivitySummaryResponse>

    @PATCH("api/tasks/{taskId}")
    suspend fun updateTask(@Path("taskId") taskId: String, @Body body: Map<String, Boolean>): Response<Task>

    @DELETE("api/tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String): Response<Unit>

    @PUT("api/events/{eventId}")
    suspend fun updateEvent(@Path("eventId") eventId: String, @Body body: EventUpdateRequest): Response<Event>

    @DELETE("api/events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: String): Response<Unit>

    @GET("api/kanban/boards")
    suspend fun getKanbanBoards(): Response<List<KanbanBoard>>

    @PATCH("api/kanban/cards/{cardId}/move")
    suspend fun moveKanbanCard(
        @Path("cardId") cardId: String,
        @Body moveRequest: KanbanCardMoveRequest
    ): Response<Unit>
}
