package id.studysynsamr.studysyns

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var taskAdapter: TaskAdapter
    
    private var allTasks: List<TaskResponse> = emptyList()
    private var selectedDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        rvTasks = view.findViewById(R.id.rv_calendar_tasks)
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)
        tvEmpty = view.findViewById(R.id.tv_empty_calendar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendar()
        fetchTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { task ->
            showTaskDetailDialog(task)
        }
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = taskAdapter
    }

    private fun showTaskDetailDialog(task: TaskResponse) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_detail, null)
        bottomSheetDialog.setContentView(dialogView)

        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_title)
        val tvStatus = dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_status)
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_date)
        val tvDesc = dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_desc)
        val btnMarkDone = dialogView.findViewById<android.widget.Button>(R.id.btn_mark_done)
        val btnEdit = dialogView.findViewById<android.widget.Button>(R.id.btn_edit_task)
        val btnDelete = dialogView.findViewById<android.widget.Button>(R.id.btn_delete_task)

        tvTitle.text = task.judulTugas
        tvDesc.text = task.deskripsi ?: "Tidak ada deskripsi"
        tvDate.text = task.batasWaktu ?: "-"
        
        if (task.statusSelesai) {
            tvStatus.text = "Selesai"
            tvStatus.setBackgroundResource(R.drawable.bg_status_done)
            btnMarkDone.visibility = View.GONE
        } else {
            tvStatus.text = "Belum Selesai"
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            btnMarkDone.visibility = View.VISIBLE
        }

        btnMarkDone.setOnClickListener {
            val map = mapOf("status_selesai" to true)
            NetworkClient.apiService.updateTaskStatus(task.id, map).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Tugas ditandai selesai", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.dismiss()
                        fetchTasks()
                    }
                }
                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
            })
        }

        btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Hapus Tugas")
                .setMessage("Apakah Anda yakin ingin menghapus tugas ini?")
                .setPositiveButton("Hapus") { _, _ ->
                    NetworkClient.apiService.deleteTask(task.id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Tugas dihapus", Toast.LENGTH_SHORT).show()
                                bottomSheetDialog.dismiss()
                                fetchTasks()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {}
                    })
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnEdit.setOnClickListener {
            bottomSheetDialog.dismiss()
            showEditTaskDialog(task)
        }

        bottomSheetDialog.show()
    }

    private fun showEditTaskDialog(task: TaskResponse) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        bottomSheetDialog.setContentView(dialogView)

        val etJudul = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_judul)
        val etDeskripsi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_deskripsi)
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tv_edit_date)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_edit)

        etJudul.setText(task.judulTugas)
        etDeskripsi.setText(task.deskripsi)
        tvDate.text = task.batasWaktu ?: "Pilih Tanggal"
        var selectedDate = task.batasWaktu

        tvDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02dT12:00:00.000Z", year, month + 1, dayOfMonth)
                selectedDate = dateStr
                tvDate.text = "$year-${month + 1}-$dayOfMonth"
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        btnSave.setOnClickListener {
            val judul = etJudul.text.toString()
            val deskripsi = etDeskripsi.text.toString()

            if (judul.isNotEmpty()) {
                val request = TaskRequest(task.userId ?: 1, judul, deskripsi, selectedDate, task.sumber)
                NetworkClient.apiService.updateTask(task.id, request).enqueue(object : Callback<TaskResponse> {
                    override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                            fetchTasks()
                        }
                    }
                    override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
                })
            }
        }

        bottomSheetDialog.show()
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.tv_day_text)
            val dotView = view.findViewById<View>(R.id.v_event_dot)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        calendarView.notifyDateChanged(day.date)
                        if (oldDate != null) {
                            calendarView.notifyDateChanged(oldDate)
                        }
                        updateTasksForSelectedDate(day.date)
                    }
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    
                    if (data.date == selectedDate) {
                        container.textView.setBackgroundResource(R.drawable.red_dot_bg) // Use dot bg as circle for selection
                        container.textView.setTextColor(resources.getColor(R.color.white, null))
                    } else {
                        container.textView.background = null
                        container.textView.setTextColor(resources.getColor(R.color.navy_dark, null))
                    }

                    // Check for tasks on this date
                    val hasTask = allTasks.any { 
                        it.batasWaktu != null && it.batasWaktu.startsWith(data.date.toString()) 
                    }
                    container.dotView.visibility = if (hasTask && data.date != selectedDate) View.VISIBLE else View.INVISIBLE
                } else {
                    container.textView.visibility = View.INVISIBLE
                    container.dotView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun fetchTasks() {
        val sharedPref = requireActivity().getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        NetworkClient.apiService.getTasks(userId).enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    allTasks = response.body()!!
                    calendarView.notifyCalendarChanged()
                    
                    // Show tasks for today if possible
                    selectedDate = LocalDate.now()
                    calendarView.notifyDateChanged(selectedDate!!)
                    updateTasksForSelectedDate(selectedDate!!)
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat tugas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error koneksi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTasksForSelectedDate(date: LocalDate) {
        val dateString = date.toString()
        val tasksForDate = allTasks.filter { it.batasWaktu != null && it.batasWaktu.startsWith(dateString) }
        
        tvSelectedDate.text = "Tugas pada " + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        
        if (tasksForDate.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvTasks.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvTasks.visibility = View.VISIBLE
            taskAdapter.updateData(tasksForDate)
        }
    }
}
