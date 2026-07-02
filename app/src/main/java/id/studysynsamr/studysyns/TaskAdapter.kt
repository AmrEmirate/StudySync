package id.studysynsamr.studysyns

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private var taskList: List<TaskResponse>,
    private val onItemClick: ((TaskResponse) -> Unit)? = null
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tv_judul)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tv_deskripsi)
        val tvTanggal: TextView = itemView.findViewById(R.id.tv_tanggal)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvJudul.text = task.judulTugas
        holder.tvDeskripsi.text = task.deskripsi ?: "Tidak ada deskripsi"

        if (task.batasWaktu != null) {
            try {
                // Backend returns ISO string e.g. "2026-07-02T12:00:00.000Z"
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = parser.parse(task.batasWaktu)
                holder.tvTanggal.text = "Batas: ${if (date != null) formatter.format(date) else task.batasWaktu}"
            } catch (e: Exception) {
                holder.tvTanggal.text = "Batas: ${task.batasWaktu}"
            }
        } else {
            holder.tvTanggal.text = "Batas: -"
        }

        if (task.statusSelesai) {
            holder.tvStatus.text = "Selesai"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done)
        } else {
            holder.tvStatus.text = "Belum Selesai"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(task)
        }
    }

    override fun getItemCount() = taskList.size

    fun updateData(newTasks: List<TaskResponse>) {
        taskList = newTasks
        notifyDataSetChanged()
    }
}
