package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String,
    val email: String,
    val password: String
)
