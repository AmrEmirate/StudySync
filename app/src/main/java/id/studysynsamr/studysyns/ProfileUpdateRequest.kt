package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val identitas: String? = null,
    @SerializedName("nama_lengkap") val namaLengkap: String? = null,
    val password: String? = null
)
