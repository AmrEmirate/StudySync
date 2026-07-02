package id.studysynsamr.studysyns

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import id.studysynsamr.studysyns.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    performGoogleLogin(idToken)
                } else {
                    Toast.makeText(this, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cek sesi login
        val sharedPref = getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Kolom email dan password dibiarkan kosong

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Email dan sandi harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun performLogin(email: String, pass: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Memuat..."

        val request = LoginRequest(email, pass)
        NetworkClient.apiService.login(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    
                    // Simpan sesi
                    val sharedPref = getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putBoolean("is_logged_in", true)
                        putInt("user_id", user.id)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "Selamat datang, ${user.namaLengkap}", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Email atau sandi salah", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"
                Toast.makeText(this@LoginActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Error API", t)
            }
        })
    }

    private fun performGoogleLogin(idToken: String) {
        binding.btnGoogleLogin.isEnabled = false
        binding.btnGoogleLogin.text = "Memuat..."

        val request = LoginRequest(googleIdToken = idToken)
        NetworkClient.apiService.login(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                binding.btnGoogleLogin.isEnabled = true
                binding.btnGoogleLogin.text = "Masuk dengan Google"

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    
                    val sharedPref = getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putBoolean("is_logged_in", true)
                        putInt("user_id", user.id)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "Selamat datang, ${user.namaLengkap}", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Login Google gagal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                binding.btnGoogleLogin.isEnabled = true
                binding.btnGoogleLogin.text = "Masuk dengan Google"
                Toast.makeText(this@LoginActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Error API Google", t)
            }
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() 
    }

    private fun showForgotPasswordDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        bottomSheetDialog.setContentView(dialogView)

        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_forgot_email)
        val btnSendOtp = dialogView.findViewById<android.widget.Button>(R.id.btn_send_otp)

        btnSendOtp.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                btnSendOtp.isEnabled = false
                btnSendOtp.text = "Mengirim..."
                
                val request = ForgotPasswordRequest(email)
                NetworkClient.apiService.forgotPassword(request).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        btnSendOtp.isEnabled = true
                        btnSendOtp.text = "Kirim Kode OTP"
                        if (response.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "OTP terkirim ke email", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                            showResetPasswordDialog(email)
                        } else {
                            Toast.makeText(this@LoginActivity, "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        btnSendOtp.isEnabled = true
                        btnSendOtp.text = "Kirim Kode OTP"
                        Toast.makeText(this@LoginActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }

    private fun showResetPasswordDialog(email: String) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        bottomSheetDialog.setContentView(dialogView)

        val etOtp = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_reset_otp)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_reset_new_password)
        val btnReset = dialogView.findViewById<android.widget.Button>(R.id.btn_reset_password)

        btnReset.setOnClickListener {
            val otp = etOtp.text.toString()
            val newPassword = etNewPassword.text.toString()

            if (otp.length == 6 && newPassword.length >= 6) {
                btnReset.isEnabled = false
                btnReset.text = "Memproses..."

                val request = ResetPasswordRequest(email, otp, newPassword)
                NetworkClient.apiService.resetPassword(request).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        btnReset.isEnabled = true
                        btnReset.text = "Reset Kata Sandi"
                        if (response.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "Kata sandi berhasil direset", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                        } else {
                            Toast.makeText(this@LoginActivity, "OTP salah", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        btnReset.isEnabled = true
                        btnReset.text = "Reset Kata Sandi"
                        Toast.makeText(this@LoginActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "OTP harus 6 digit & sandi minimal 6 karakter", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }
}
