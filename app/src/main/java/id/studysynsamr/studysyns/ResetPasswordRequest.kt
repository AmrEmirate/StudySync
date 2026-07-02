package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    @SerializedName("new_password") val newPassword: String
)
