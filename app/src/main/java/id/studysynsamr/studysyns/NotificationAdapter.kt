package id.studysynsamr.studysyns

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notifList: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_notif_title)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_notif_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val item = notifList[position]
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
    }

    override fun getItemCount(): Int = notifList.size
    
    fun removeItem(position: Int) {
        notifList.removeAt(position)
        notifyItemRemoved(position)
    }
}
