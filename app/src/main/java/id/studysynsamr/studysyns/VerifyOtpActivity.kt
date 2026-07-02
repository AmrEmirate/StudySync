package id.studysynsamr.studysyns

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        val email = intent.getStringExtra("email") ?: return finish()
        
        val etOtp = findViewById<TextInputEditText>(R.id.et_otp)
        val btnVerify = findViewById<Button>(R.id.btn_verify)

        btnVerify.setOnClickListener {
            val otp = etOtp.text.toString()
            if (otp.length < 6) {
                Toast.makeText(this, "OTP harus 6 digit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnVerify.isEnabled = false
            btnVerify.text = "Memverifikasi..."

            val request = VerifyOtpRequest(email, otp)
            NetworkClient.apiService.verifyOtp(request).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    btnVerify.isEnabled = true
                    btnVerify.text = "Verifikasi"

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@VerifyOtpActivity, "Akun berhasil diverifikasi!", Toast.LENGTH_SHORT).show()
                        val user = response.body()!!
                        
                        // Auto login
                        val sharedPref = getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putBoolean("is_logged_in", true)
                            putInt("user_id", user.id)
                            apply()
                        }
                        
                        val intent = Intent(this@VerifyOtpActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@VerifyOtpActivity, "OTP Salah atau Invalid", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    btnVerify.isEnabled = true
                    btnVerify.text = "Verifikasi"
                    Toast.makeText(this@VerifyOtpActivity, "Error koneksi", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
