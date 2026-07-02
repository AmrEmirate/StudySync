package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class TaskRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("judul_tugas") val judulTugas: String,
    val deskripsi: String?,
    @SerializedName("batas_waktu") val batasWaktu: String?,
    val sumber: String = "manual",
    val status: String? = null
)
