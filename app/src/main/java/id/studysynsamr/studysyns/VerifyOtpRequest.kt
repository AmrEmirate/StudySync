package id.studysynsamr.studysyns

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)
