package id.studysynsamr.studysyns

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import id.studysynsamr.studysyns.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter
    private var allTasks: List<TaskResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupTabs()
        setupFab()
        fetchTasks()
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.ivShareAll.setOnClickListener {
            shareAllTasks()
        }
    }

    private fun shareAllTasks() {
        if (allTasks.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada tugas untuk dibagikan", Toast.LENGTH_SHORT).show()
            return
        }

        val shareBuilder = java.lang.StringBuilder("Daftar Tugas Saya:\n\n")
        allTasks.forEachIndexed { index, task ->
            val dateStr = formatIsoDate(task.batasWaktu)
            val statusStr = when(task.status) {
                "SELESAI" -> "Selesai"
                "PROSES" -> "Sedang Proses"
                else -> "Belum Selesai"
            }
            shareBuilder.append("${index + 1}. ${task.judulTugas}\n")
            shareBuilder.append("   Batas: $dateStr\n")
            shareBuilder.append("   Status: $statusStr\n\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareBuilder.toString().trimEnd())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan semua tugas ke...")
        startActivity(shareIntent)
    }

    private fun showAddTaskDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        bottomSheetDialog.setContentView(dialogView)

        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_task_title)
        val etDesc = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_task_desc)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_task)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val desc = etDesc.text.toString()

            if (title.isNotEmpty()) {
                val sharedPref = requireActivity().getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 1)
                
                val request = TaskRequest(userId, title, desc, null, "manual") // TODO: Handle Date/Time picker later
                NetworkClient.apiService.createTask(request).enqueue(object : Callback<TaskResponse> {
                    override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Tugas ditambahkan", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                            fetchTasks()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menambahkan tugas", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(emptyList()) { task ->
            showTaskDetailDialog(task)
        }
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTasks.adapter = adapter
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
        tvDate.text = formatIsoDate(task.batasWaktu)
        
        if (task.status == "SELESAI") {
            tvStatus.text = "Selesai"
            tvStatus.setBackgroundResource(R.drawable.bg_status_done)
        } else if (task.status == "PROSES") {
            tvStatus.text = "Sedang Proses"
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
        } else {
            tvStatus.text = "Belum Selesai"
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
        }
        btnMarkDone.visibility = View.VISIBLE

        btnMarkDone.setOnClickListener {
            val statusOptions = arrayOf("Belum Selesai", "Sedang Proses", "Selesai")
            val statusValues = arrayOf("BELUM_SELESAI", "PROSES", "SELESAI")
            var checkedItem = statusValues.indexOf(task.status)
            if (checkedItem == -1) checkedItem = 0

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Ubah Status Tugas")
                .setSingleChoiceItems(statusOptions, checkedItem) { dialog, which ->
                    val selectedStatus = statusValues[which]
                    val map = mapOf("status" to selectedStatus)
                    NetworkClient.apiService.updateTaskStatus(task.id, map).enqueue(object : Callback<TaskResponse> {
                        override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Status diperbarui", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                bottomSheetDialog.dismiss()
                                fetchTasks()
                            }
                        }
                        override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
                    })
                }
                .setNegativeButton("Batal", null)
                .show()
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

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterTasks(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterTasks(tabIndex: Int) {
        val filteredList = when (tabIndex) {
            0 -> allTasks.filter { it.status == "BELUM_SELESAI" } // Belum Selesai
            1 -> allTasks.filter { it.status == "PROSES" } // Proses
            2 -> allTasks.filter { it.status == "SELESAI" } // Selesai
            else -> allTasks
        }

        if (filteredList.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerViewTasks.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerViewTasks.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    private fun fetchTasks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewTasks.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        // Ambil user ID dari sesi
        val sharedPref = requireActivity().getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1) 

        NetworkClient.apiService.getTasks(userId).enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    allTasks = response.body()!!
                    filterTasks(binding.tabLayout.selectedTabPosition)
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "API Error", t)
            }
        })
    }

    private fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "-"
        return try {
            val zonedDateTime = java.time.ZonedDateTime.parse(isoDate)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
