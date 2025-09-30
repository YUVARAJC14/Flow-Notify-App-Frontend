package com.saveetha.flownotify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.*
import kotlinx.coroutines.launch
import java.util.Collections

class KanbanActivity : AppCompatActivity() {

    private lateinit var rvKanbanColumns: RecyclerView
    private lateinit var columnAdapter: KanbanColumnAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kanban)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val dragCallback = DragAndDropCallback { /* Drag completed */ }
        itemTouchHelper = ItemTouchHelper(dragCallback)

        rvKanbanColumns = findViewById(R.id.rv_kanban_columns)
        rvKanbanColumns.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        columnAdapter = KanbanColumnAdapter(mutableListOf(), itemTouchHelper, this::moveCard, this::showCompletionDialog)
        rvKanbanColumns.adapter = columnAdapter

        loadKanbanData()
    }

    private fun showCompletionDialog(card: KanbanCard) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_details, null)
        val dialog = AlertDialog.Builder(this, R.style.AlertDialog_Transparent).setView(dialogView).create()

        val title = dialogView.findViewById<TextView>(R.id.tv_task_details_title)
        val description = dialogView.findViewById<TextView>(R.id.tv_task_details_description)
        val completeButton = dialogView.findViewById<Button>(R.id.btn_complete_task)
        val deleteButton = dialogView.findViewById<Button>(R.id.btn_delete_task)
        deleteButton.visibility = View.GONE // Hide delete button for now

        if (card.task != null) {
            title.text = card.task.title
            description.text = card.task.description ?: "No description available."
        } else if (card.event != null) {
            title.text = card.event.title
            description.text = card.event.notes ?: "No notes available."
        }

        completeButton.setOnClickListener {
            handleCardCompletion(card)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleCardCompletion(card: KanbanCard) {
        lifecycleScope.launch {
            try {
                val isSuccess = if (card.task != null) {
                    apiService.updateTask(card.task.id, mapOf("isCompleted" to true)).isSuccessful
                } else if (card.event != null) {
                    apiService.updateEvent(card.event.id, EventUpdateRequest(completed = true)).isSuccessful
                } else {
                    false
                }

                if (isSuccess) {
                    Toast.makeText(this@KanbanActivity, "Item marked as complete", Toast.LENGTH_SHORT).show()
                    val doneColumn = columnAdapter.columnsData.find { it.name.equals("Done", ignoreCase = true) }
                    if (doneColumn != null) {
                        moveCard(card.id, doneColumn.id, 0)
                        loadKanbanData()
                    } else {
                        showError("Could not find 'Done' column.")
                    }
                } else {
                    showError("Failed to mark item as complete.")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun moveCard(cardId: String, newColumnId: String, newPosition: Int) {
        lifecycleScope.launch {
            try {
                val request = KanbanCardMoveRequest(newColumnId = newColumnId, position = newPosition)
                val response = apiService.moveKanbanCard(cardId, request)
                if (!response.isSuccessful) {
                    showError("Failed to save card move. Please try again.")
                    loadKanbanData() // Revert UI change
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
                loadKanbanData()
            }
        }
    }

    private fun loadKanbanData() {
        lifecycleScope.launch {
            try {
                val response = apiService.getKanbanBoards()
                if (response.isSuccessful) {
                    val boards = response.body()
                    boards?.firstOrNull()?.let {
                        columnAdapter.updateData(it.columns.toMutableList())
                        // After data is loaded, check for auto-moves
                        checkForScheduledMoves()
                    } ?: showError("No Kanban boards found.")
                } else {
                    showError("Failed to load Kanban board: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun checkForScheduledMoves() {
        val todoColumn = columnAdapter.columnsData.find { it.name.equals("To Do", ignoreCase = true) }
        val inProgressColumn = columnAdapter.columnsData.find { it.name.equals("In Progress", ignoreCase = true) }

        if (todoColumn == null || inProgressColumn == null) {
            return // Can't perform move if columns don't exist
        }

        val cardsToMove = mutableListOf<KanbanCard>()
        val now = java.util.Calendar.getInstance()

        for (card in todoColumn.cards) {
            val itemCalendar = java.util.Calendar.getInstance()
            try {
                if (card.task != null && card.task.dueDate != null && card.task.time != null) {
                    // Assuming format YYYY-MM-DD and HH:mm:ss
                    val dateTimeString = "${card.task.dueDate} ${card.task.time}"
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    itemCalendar.time = sdf.parse(dateTimeString) ?: continue
                } else if (card.event != null) {
                    val dateTimeString = "${card.event.date} ${card.event.startTime}"
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    itemCalendar.time = sdf.parse(dateTimeString) ?: continue
                } else {
                    continue
                }

                // Check if the item is scheduled to start within the next 30 minutes
                val diffMillis = itemCalendar.timeInMillis - now.timeInMillis
                if (diffMillis in 1..30 * 60 * 1000) { // 1ms to 30 minutes
                    cardsToMove.add(card)
                }

            } catch (e: java.text.ParseException) {
                // Ignore cards with unparseable dates
                continue
            }
        }

        if (cardsToMove.isNotEmpty()) {
            lifecycleScope.launch {
                for (card in cardsToMove) {
                    moveCard(card.id, inProgressColumn.id, 0) // Move to top of In Progress
                }
                // Refresh board after all moves are requested
                loadKanbanData()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

class KanbanColumnAdapter(
    private var columns: MutableList<KanbanColumn>,
    private val itemTouchHelper: ItemTouchHelper,
    private val onCardMove: (cardId: String, newColumnId: String, newPosition: Int) -> Unit,
    private val onCardClick: (card: KanbanCard) -> Unit
) : RecyclerView.Adapter<KanbanColumnAdapter.ViewHolder>() {

    val columnsData: List<KanbanColumn> get() = columns

    fun updateData(newColumns: MutableList<KanbanColumn>) {
        columns = newColumns.sortedBy { it.position }.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kanban_column, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(columns[position], itemTouchHelper, onCardMove, onCardClick)
    }

    override fun getItemCount() = columns.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_column_title)
        val cardsRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_kanban_cards)

        fun bind(
            column: KanbanColumn,
            itemTouchHelper: ItemTouchHelper,
            onCardMove: (cardId: String, newColumnId: String, newPosition: Int) -> Unit,
            onCardClick: (card: KanbanCard) -> Unit
        ) {
            title.text = column.name
            cardsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            val cardAdapter = KanbanCardAdapter(column.cards.toMutableList(), column.id, onCardMove, onCardClick)
            cardsRecyclerView.adapter = cardAdapter
            itemTouchHelper.attachToRecyclerView(cardsRecyclerView)
        }
    }
}

class KanbanCardAdapter(
    private var cards: MutableList<KanbanCard>,
    private val columnId: String,
    private val onCardMove: (cardId: String, newColumnId: String, newPosition: Int) -> Unit,
    private val onCardClick: (card: KanbanCard) -> Unit
) : RecyclerView.Adapter<KanbanCardAdapter.ViewHolder>(), DragAndDropListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kanban_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
        holder.itemView.setOnClickListener { onCardClick(card) }
    }

    override fun getItemCount() = cards.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) { Collections.swap(cards, i, i + 1) }
        } else {
            for (i in fromPosition downTo toPosition + 1) { Collections.swap(cards, i, i - 1) }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDrop(fromPosition: Int, toPosition: Int) {
        val card = cards[toPosition]
        onCardMove(card.id, columnId, toPosition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_card_title)
        private val description: TextView = itemView.findViewById(R.id.tv_card_description)

        fun bind(card: KanbanCard) {
            if (card.task != null) {
                title.text = card.task.title
                description.text = card.task.description ?: ""
            } else if (card.event != null) {
                title.text = card.event.title
                description.text = card.event.notes ?: ""
            }
        }
    }
}

interface DragAndDropListener {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDrop(fromPosition: Int, toPosition: Int)
}

class DragAndDropCallback(private val onDragComplete: () -> Unit) : ItemTouchHelper.Callback() {

    private var fromPosition: Int = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition
        val adapter = recyclerView.adapter as DragAndDropListener
        adapter.onItemMove(fromPos, toPos)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fromPosition = viewHolder?.adapterPosition ?: -1
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val toPosition = viewHolder.adapterPosition
        if (fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
            val adapter = recyclerView.adapter as DragAndDropListener
            adapter.onItemDrop(fromPosition, toPosition)
        }
        fromPosition = -1 // Reset position
        onDragComplete()
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
}

