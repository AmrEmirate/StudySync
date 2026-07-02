package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    @SerializedName("google_id_token") val googleIdToken: String? = null
)
