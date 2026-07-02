package id.studysynsamr.studysyns

import com.google.gson.annotations.SerializedName

data class SchoolIntegrationResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("endpoint_url") val endpointUrl: String,
    @SerializedName("auth_token") val authToken: String
)
