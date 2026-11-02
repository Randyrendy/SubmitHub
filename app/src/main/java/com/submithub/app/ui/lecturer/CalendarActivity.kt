package com.submithub.app.ui.lecturer

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.CalendarEventEntity
import com.submithub.app.data.entity.CodeEntity
import com.submithub.app.databinding.ActivityCalendarBinding
import com.submithub.app.databinding.DialogAddEventBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var repository: Repository
    private lateinit var lecturerId: String

    private val displayedMonth: Calendar = Calendar.getInstance()
    private var selectedDayMillis: Long = startOfDay(System.currentTimeMillis())

    private var allCodes: List<CodeEntity> = emptyList()
    private var allEvents: List<CalendarEventEntity> = emptyList()

    private data class CalItem(
        val title: String,
        val subtitle: String,
        val isDeadline: Boolean,
        val codeId: String? = null,
        val eventId: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        lecturerId = intent.getStringExtra("lecturerId") ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnPrevMonth.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, -1)
            renderGrid()
        }
        binding.btnNextMonth.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, 1)
            renderGrid()
        }
        binding.btnAddEvent.setOnClickListener { showAddEventDialog() }

        repository.codesByLecturer(lecturerId).observe(this) { codes ->
            allCodes = codes
            renderGrid()
            renderSelectedDayEvents()
        }
        repository.eventsForLecturer(lecturerId).observe(this) { events ->
            allEvents = events
            renderGrid()
            renderSelectedDayEvents()
        }

        renderGrid()
        renderSelectedDayEvents()
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun itemsByDay(): Map<Long, List<CalItem>> {
        val map = mutableMapOf<Long, MutableList<CalItem>>()

        allCodes.forEach { code ->
            val day = startOfDay(code.expiresAt)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val item = CalItem(
                title = "${code.assignmentTitle} due",
                subtitle = "${code.courseCode} · ${timeFormat.format(code.expiresAt)}",
                isDeadline = true,
                codeId = code.id
            )
            map.getOrPut(day) { mutableListOf() }.add(item)
        }

        allEvents.forEach { event ->
            val day = startOfDay(event.dateMillis)
            val item = CalItem(
                title = event.title,
                subtitle = event.notes,
                isDeadline = false,
                eventId = event.id
            )
            map.getOrPut(day) { mutableListOf() }.add(item)
        }

        return map
    }

    private fun renderGrid() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = monthFormat.format(displayedMonth.time)

        binding.gridContainer.removeAllViews()

        val eventsByDay = itemsByDay()
        val today = startOfDay(System.currentTimeMillis())

        val monthStart = displayedMonth.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        val firstWeekday = monthStart.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)

        val totalCells = firstWeekday + daysInMonth
        val totalRows = (totalCells + 6) / 7

        var dayCounter = 1

        for (rowIndex in 0 until totalRows) {
            val currentRow = LinearLayout(this)
            currentRow.orientation = LinearLayout.HORIZONTAL
            currentRow.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            for (col in 0 until 7) {
                val cellIndex = rowIndex * 7 + col
                val cellView = LayoutInflater.from(this).inflate(R.layout.item_calendar_day, currentRow, false)
                val tvDay = cellView.findViewById<TextView>(R.id.tvDayNumber)
                val dot = cellView.findViewById<View>(R.id.dotIndicator)

                if (cellIndex < firstWeekday || dayCounter > daysInMonth) {
                    tvDay.text = ""
                    dot.visibility = View.INVISIBLE
                    cellView.isClickable = false
                } else {
                    val dayNumber = dayCounter
                    tvDay.text = dayNumber.toString()

                    val cellCal = monthStart.clone() as Calendar
                    cellCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                    val cellMillis = startOfDay(cellCal.timeInMillis)

                    val hasEvents = eventsByDay.containsKey(cellMillis)
                    dot.visibility = if (hasEvents) View.VISIBLE else View.INVISIBLE

                    when {
                        cellMillis == selectedDayMillis -> {
                            tvDay.setBackgroundResource(R.drawable.bg_day_selected_circle)
                            tvDay.setTextColor(getColor(R.color.white))
                        }
                        cellMillis == today -> {
                            tvDay.setBackgroundResource(R.drawable.bg_icon_circle)
                            tvDay.setTextColor(getColor(R.color.primary_dark))
                        }
                        else -> {
                            tvDay.background = null
                            tvDay.setTextColor(getColor(R.color.text_primary))
                        }
                    }

                    cellView.setOnClickListener {
                        selectedDayMillis = cellMillis
                        renderGrid()
                        renderSelectedDayEvents()
                    }

                    dayCounter++
                }

                currentRow.addView(cellView)
            }
            binding.gridContainer.addView(currentRow)
        }
    }

    private fun renderSelectedDayEvents() {
        val label = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(selectedDayMillis)
        binding.tvSelectedDateLabel.text = label

        val items = itemsByDay()[selectedDayMillis].orEmpty()
        binding.containerEvents.removeAllViews()

        if (items.isEmpty()) {
            binding.tvNoEvents.visibility = View.VISIBLE
            binding.containerEvents.visibility = View.GONE
            return
        }

        binding.tvNoEvents.visibility = View.GONE
        binding.containerEvents.visibility = View.VISIBLE

        items.forEach { item ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_calendar_event, binding.containerEvents, false)
            row.findViewById<TextView>(R.id.tvTitle).text = item.title
            row.findViewById<TextView>(R.id.tvSubtitle).text = item.subtitle
            row.findViewById<ImageView>(R.id.ivIcon).setImageResource(
                if (item.isDeadline) R.drawable.ic_assignment else R.drawable.ic_calendar
            )

            if (item.isDeadline) {
                row.setOnClickListener {
                    val intent = Intent(this, SubmissionsActivity::class.java)
                    intent.putExtra("codeId", item.codeId)
                    startActivity(intent)
                }
            } else {
                val deleteBtn = row.findViewById<View>(R.id.btnDelete)
                deleteBtn.visibility = View.VISIBLE
                deleteBtn.setOnClickListener {
                    lifecycleScope.launch {
                        item.eventId?.let { repository.deleteEvent(it) }
                    }
                }
            }

            binding.containerEvents.addView(row)
        }
    }

    private fun showAddEventDialog() {
        val dialogBinding = DialogAddEventBinding.inflate(layoutInflater)
        var chosenDateMillis = selectedDayMillis

        fun refreshDateLabel() {
            dialogBinding.tvSelectedDate.text =
                "Date: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(chosenDateMillis)}"
        }
        refreshDateLabel()

        dialogBinding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = chosenDateMillis
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val picked = Calendar.getInstance()
                    picked.set(year, month, day, 0, 0, 0)
                    chosenDateMillis = startOfDay(picked.timeInMillis)
                    refreshDateLabel()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val notes = dialogBinding.etNotes.text.toString().trim()

            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Please enter a title"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                repository.createEvent(
                    CalendarEventEntity(
                        lecturerId = lecturerId,
                        title = title,
                        dateMillis = chosenDateMillis,
                        notes = notes
                    )
                )
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}