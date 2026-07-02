package id.studysynsamr.studysyns

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<TextInputEditText>(R.id.et_name)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)

        tvLoginLink.setOnClickListener {
            finish() // go back to login
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Memuat..."

            val request = RegisterRequest(name, email, pass)
            NetworkClient.apiService.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Daftar"

                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "OTP dikirim ke email", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Gagal mendaftar. Email mungkin sudah ada.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Daftar"
                    Toast.makeText(this@RegisterActivity, "Error koneksi", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
