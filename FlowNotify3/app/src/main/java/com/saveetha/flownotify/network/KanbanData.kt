package com.saveetha.flownotify.network

import com.google.gson.annotations.SerializedName

data class KanbanBoard(
    val id: String,
    val name: String,
    @SerializedName("owner_id") val ownerId: String,
    val columns: List<KanbanColumn> = emptyList()
)

data class KanbanColumn(
    val id: String,
    val name: String,
    val position: Int,
    @SerializedName("board_id") val boardId: String,
    val cards: List<KanbanCard> = emptyList()
)

data class KanbanCard(
    val id: String,
    val position: Int,
    @SerializedName("column_id") val columnId: String,
    @SerializedName("task_id") val taskId: String?,
    @SerializedName("event_id") val eventId: String?,
    val task: Task?,
    val event: Event?
)

data class KanbanCardMoveRequest(
    @SerializedName("new_column_id") val newColumnId: String,
    val position: Int
)
