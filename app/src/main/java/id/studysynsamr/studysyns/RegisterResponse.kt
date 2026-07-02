package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val message: String,
    @SerializedName("user_id") val userId: Int
)
