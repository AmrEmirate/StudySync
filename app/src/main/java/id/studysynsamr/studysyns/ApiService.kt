package id.studysynsamr.studysyns

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<UserResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/verify")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<UserResponse>

    @GET("api/user/{id}")
    fun getUserProfile(@Path("id") userId: Int): Call<UserResponse>

    @GET("api/tasks/{userId}")
    fun getTasks(@Path("userId") userId: Int): Call<List<TaskResponse>>

    @POST("api/tasks")
    fun createTask(@Body request: TaskRequest): Call<TaskResponse>

    @retrofit2.http.PUT("api/tasks/{id}")
    fun updateTask(@Path("id") taskId: Int, @Body request: TaskRequest): Call<TaskResponse>

    @retrofit2.http.DELETE("api/tasks/{id}")
    fun deleteTask(@Path("id") taskId: Int): Call<Void>

    @POST("api/school-integration")
    fun updateSchoolIntegration(@Body request: SchoolIntegrationRequest): Call<SchoolIntegrationResponse>

    @GET("api/school-integration/{userId}")
    fun getSchoolIntegration(@Path("userId") userId: Int): Call<SchoolIntegrationResponse>

    @retrofit2.http.PUT("api/user/{id}")
    fun updateUserProfile(@Path("id") userId: Int, @Body request: ProfileUpdateRequest): Call<UserResponse>

    @retrofit2.http.PUT("api/tasks/{id}/status")
    fun updateTaskStatus(@Path("id") taskId: Int, @Body request: Map<String, Boolean>): Call<TaskResponse>
}
