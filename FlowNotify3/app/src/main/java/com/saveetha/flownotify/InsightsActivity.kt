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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView

class InsightsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var heatmapContainer: LinearLayout
    private lateinit var filterDay: TextView
    private lateinit var filterWeek: TextView
    private lateinit var filterMonth: TextView
    private lateinit var filterYear: TextView

    // Example data
    private val taskCompletionData = listOf(7, 9, 11, 6, 8, 5, 4)
    private val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        initViews()
        setupBottomNavigation()
        setupFilterListeners()
        setupBarChart()
        setupHeatmap()
    }

    private fun initViews() {
        barChart = findViewById(R.id.bar_chart_task_completion)
        heatmapContainer = findViewById(R.id.heatmap_container)
        filterDay = findViewById(R.id.filter_day)
        filterWeek = findViewById(R.id.filter_week)
        filterMonth = findViewById(R.id.filter_month)
        filterYear = findViewById(R.id.filter_year)
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
        // In a real app, you'd fetch data from your backend for the selected timeframe
        // For this example, we'll just update the chart with the same data
        setupBarChart()
    }

    private fun setupBarChart() {
        val entries = ArrayList<BarEntry>()

        // Add data points
        for (i in taskCompletionData.indices) {
            entries.add(BarEntry(i.toFloat(), taskCompletionData[i].toFloat()))
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
        xAxis.valueFormatter = IndexAxisValueFormatter(weekDays)
        xAxis.granularity = 1f

        // Style Y axis
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 12f
        leftAxis.granularity = 3f

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        // Animate and refresh
        barChart.animateY(500)
        barChart.invalidate()
    }

    private fun setupHeatmap() {
        // Time slots (24-hour format)
        val timeSlots = listOf("9", "12", "15", "18", "21")

        // Example productivity data (random)
        // 0 = no data, 1 = low, 2 = medium, 3 = high productivity
        val productivityData = arrayOf(
            intArrayOf(0, 3, 3, 0, 3, 2, 1), // 9 AM
            intArrayOf(2, 1, 2, 3, 0, 3, 2), // 12 PM
            intArrayOf(3, 0, 1, 2, 3, 0, 0), // 3 PM
            intArrayOf(1, 2, 0, 1, 2, 1, 0), // 6 PM
            intArrayOf(0, 0, 1, 0, 0, 0, 1)  // 9 PM
        )

        // Create rows for each time slot
        for (timeIndex in timeSlots.indices) {
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
            timeLabel.text = timeSlots[timeIndex]
            timeLabel.textSize = 12f
            timeLabel.setTextColor(Color.parseColor("#9E9E9E"))
            row.addView(timeLabel)

            // Add cells for each day
            for (dayIndex in 0..6) {
                val cell = View(this)
                val cellParams = LinearLayout.LayoutParams(
                    0, // Width (will be weighted)
                    48  // Height in dp
                )
                cellParams.weight = 1f
                cellParams.setMargins(4, 4, 4, 4)
                cell.layoutParams = cellParams

                // Set background color based on productivity level
                val productivityLevel = productivityData[timeIndex][dayIndex]
                val backgroundColor = when (productivityLevel) {
                    0 -> Color.parseColor("#F5F5F5") // No data
                    1 -> Color.parseColor("#BBDEFB") // Low
                    2 -> Color.parseColor("#64B5F6") // Medium
                    3 -> Color.parseColor("#2196F3") // High
                    else -> Color.parseColor("#F5F5F5")
                }
                cell.setBackgroundColor(backgroundColor)

                row.addView(cell)
            }

            heatmapContainer.addView(row)
        }
    }
}