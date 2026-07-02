package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    val email: String,
    @SerializedName("profile_picture") val profilePicture: String?,
    val identitas: String?,
    @SerializedName("preferensi_notifikasi") val preferensiNotifikasi: Boolean
)
