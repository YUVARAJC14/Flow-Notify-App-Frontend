package com.saveetha.flownotify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.InsightsResponse
import com.saveetha.flownotify.network.TaskCompletion
import kotlinx.coroutines.launch

class InsightsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var heatmapContainer: LinearLayout
    private lateinit var filterDay: TextView
    private lateinit var filterWeek: TextView
    private lateinit var filterMonth: TextView
    private lateinit var filterYear: TextView
    private lateinit var flowScoreTextView: TextView
    private lateinit var flowScoreMessageTextView: TextView
    private lateinit var flowPieChart: PieChartView

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        initViews()
        setupBottomNavigation()
        setupFilterListeners()
        updateDataForTimeframe("week")
    }

    private fun initViews() {
        barChart = findViewById(R.id.bar_chart_task_completion)
        heatmapContainer = findViewById(R.id.heatmap_container)
        filterDay = findViewById(R.id.filter_day)
        filterWeek = findViewById(R.id.filter_week)
        filterMonth = findViewById(R.id.filter_month)
        filterYear = findViewById(R.id.filter_year)
        flowScoreTextView = findViewById(R.id.tv_flow_score)
        flowScoreMessageTextView = findViewById(R.id.tv_flow_score_message)
        flowPieChart = findViewById(R.id.pie_chart_flow)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_insights

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, MyTasksActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_insights -> true // Already on Insights
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFilterListeners() {
        val filters = listOf(filterDay, filterWeek, filterMonth, filterYear)

        // Set click listeners for each filter
        filters.forEach { filter ->
            filter.setOnClickListener {
                // Reset all filters
                resetFilters(filters)

                // Set the selected filter
                it.setBackgroundResource(R.drawable.bg_filter_selected)
                (it as TextView).setTextColor(ContextCompat.getColor(this, R.color.white))

                // Update data based on selected filter
                when (it.id) {
                    R.id.filter_day -> updateDataForTimeframe("day")
                    R.id.filter_week -> updateDataForTimeframe("week")
                    R.id.filter_month -> updateDataForTimeframe("month")
                    R.id.filter_year -> updateDataForTimeframe("year")
                }
            }
        }
    }

    private fun resetFilters(filters: List<TextView>) {
        filters.forEach {
            it.setBackgroundResource(android.R.color.transparent)
            it.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun updateDataForTimeframe(timeframe: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.getInsights(timeframe)
                if (response.isSuccessful) {
                    val insights = response.body()
                    insights?.let {
                        flowScoreTextView.text = "${it.flowScore.score}%"
                        flowPieChart.setPercentage(it.flowScore.score.toFloat())

                        flowScoreMessageTextView.text = when {
                            it.flowScore.score >= 80 -> "Excellent flow! Keep up the great work."
                            it.flowScore.score >= 60 -> "Good flow! You're on track."
                            it.flowScore.score >= 40 -> "Steady progress. Aim for higher flow."
                            else -> "Let's boost your flow! Focus on key tasks."
                        }

                        setupBarChart(it.taskCompletion)
                        setupHeatmap(it.productiveTimes)
                    }
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private fun setupBarChart(taskCompletion: List<TaskCompletion>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        taskCompletion.forEachIndexed { index, completion ->
            entries.add(BarEntry(index.toFloat(), completion.completed.toFloat()))
            labels.add(completion.label)
        }

        val dataSet = BarDataSet(entries, "Tasks Completed")
        dataSet.color = ContextCompat.getColor(this, R.color.green)
        dataSet.setDrawValues(false)

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        barChart.data = data

        // Style the chart
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setDrawValueAboveBar(false)

        // Style X axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = Color.parseColor("#E0E0E0")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f

        // Style Y axis
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f
        leftAxis.setLabelCount(5, true) // Show 5 labels, force integers
        leftAxis.axisLineColor = Color.parseColor("#E0E0E0")
        leftAxis.textColor = Color.parseColor("#757575")
        leftAxis.textSize = 10f
        leftAxis.axisMaximum = (taskCompletion.maxOfOrNull { it.total }?.toFloat() ?: 10f) * 1.2f // Dynamic max
        leftAxis.setDrawLabels(true)
        leftAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        // Animate and refresh
        barChart.animateY(500)
        barChart.invalidate()
    }

    private fun setupHeatmap(productiveTimes: List<com.saveetha.flownotify.network.ProductiveTime>) {
        heatmapContainer.removeAllViews()

        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val timeSlots = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21) // Example hours

        // Create header row for days of the week
        val headerRow = LinearLayout(this)
        headerRow.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.orientation = LinearLayout.HORIZONTAL

        // Empty corner for time labels
        val cornerView = View(this)
        cornerView.layoutParams = LinearLayout.LayoutParams(48, ViewGroup.LayoutParams.WRAP_CONTENT)
        headerRow.addView(cornerView)

        for (day in daysOfWeek) {
            val dayLabel = TextView(this)
            dayLabel.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            dayLabel.gravity = Gravity.CENTER
            dayLabel.text = day
            dayLabel.textSize = 12f
            dayLabel.setTextColor(Color.parseColor("#9E9E9E"))
            headerRow.addView(dayLabel)
        }
        heatmapContainer.addView(headerRow)

        // Create rows for each time slot
        for (timeHour in timeSlots) {
            val row = LinearLayout(this)
            row.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            row.orientation = LinearLayout.HORIZONTAL

            // Add time label
            val timeLabel = TextView(this)
            timeLabel.layoutParams = LinearLayout.LayoutParams(
                48, // Width in dp
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            timeLabel.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            timeLabel.setPadding(0, 0, 8, 0)
            timeLabel.text = if (timeHour < 12) "${timeHour} AM" else if (timeHour == 12) "12 PM" else "${timeHour - 12} PM"
            timeLabel.textSize = 12f
            timeLabel.setTextColor(Color.parseColor("#9E9E9E"))
            row.addView(timeLabel)

            // Add cells for each day
            for (dayIndex in 0..6) { // 0=Monday, 1=Tuesday, ..., 6=Sunday
                val cell = View(this)
                val cellParams = LinearLayout.LayoutParams(
                    0, // Width (will be weighted)
                    48  // Height in dp
                )
                cellParams.weight = 1f
                cellParams.setMargins(4, 4, 4, 4)
                cell.layoutParams = cellParams

                val intensity = productiveTimes.find { it.day == dayIndex && it.hour == timeHour }?.intensity ?: 0f

                // Set background color based on productivity level
                val backgroundColor = when {
                    intensity > 0.7 -> ContextCompat.getColor(this, R.color.blue) // High
                    intensity > 0.4 -> ContextCompat.getColor(this, R.color.primary_blue) // Medium
                    intensity > 0 -> ContextCompat.getColor(this, R.color.light_gray) // Low
                    else -> Color.parseColor("#F5F5F5") // No data
                }
                cell.setBackgroundColor(backgroundColor)

                row.addView(cell)
            }

            heatmapContainer.addView(row)
        }
    }
}