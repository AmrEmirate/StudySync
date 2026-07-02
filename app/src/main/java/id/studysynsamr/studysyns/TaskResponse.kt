package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("judul_tugas") val judulTugas: String,
    val deskripsi: String?,
    @SerializedName("batas_waktu") val batasWaktu: String?,
    val status: String,
    val sumber: String
)
