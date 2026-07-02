package id.studysynsamr.studysyns

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.studysynsamr.studysyns.databinding.FragmentProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUserId: Int = -1
    private var currentIdentitasRaw: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivProfilePicture.setImageBitmap(bitmap)
                
                val base64Image = encodeImageToBase64(bitmap)
                updateUserProfile(profilePicture = base64Image)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Image Pick Error", e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvProfileName.text = "Memuat..."
        binding.tvProfileEmail.text = "Memuat..."

        val sharedPref = requireActivity().getSharedPreferences("StudySynsPrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId != -1) {
            loadUserProfile()
            loadSchoolIntegration()
        }

        binding.ivProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSetupApi.setOnClickListener {
            showSetupApiDialog(currentUserId)
        }
        
        binding.btnEditIdentitas.setOnClickListener {
            showEditIdentitasDialog()
        }
        
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnLogout.setOnClickListener {
            with(sharedPref.edit()) {
                clear()
                apply()
            }
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        NetworkClient.apiService.getUserProfile(currentUserId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.tvProfileName.text = user.namaLengkap
                    binding.tvProfileEmail.text = user.email
                    
                    user.identitas?.let {
                        currentIdentitasRaw = it
                        try {
                            val jsonObj = JSONObject(it)
                            val sekolah = jsonObj.optString("sekolah", "")
                            val nim = jsonObj.optString("nim", "")
                            binding.tvIdentitasInfo.text = "Sekolah: $sekolah\nNIM/NISN: $nim"
                        } catch(e: Exception) {
                            binding.tvIdentitasInfo.text = it
                        }
                    }

                    user.profilePicture?.let { base64 ->
                        try {
                            val decodedString: ByteArray = Base64.decode(base64, Base64.DEFAULT)
                            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            binding.ivProfilePicture.setImageBitmap(decodedByte)
                        } catch (e: Exception) {
                            Log.e("ProfileFragment", "Base64 decode error", e)
                        }
                    }
                } else {
                    binding.tvProfileName.text = "Gagal memuat profil"
                    binding.tvProfileEmail.text = "-"
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                binding.tvProfileName.text = "Error koneksi"
                binding.tvProfileEmail.text = "-"
                Log.e("ProfileFragment", "Error", t)
            }
        })
    }

    private fun loadSchoolIntegration() {
        NetworkClient.apiService.getSchoolIntegration(currentUserId).enqueue(object : Callback<SchoolIntegrationResponse> {
            override fun onResponse(call: Call<SchoolIntegrationResponse>, response: Response<SchoolIntegrationResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val integration = response.body()!!
                    binding.tvApiInfo.text = "Terhubung ke: ${integration.endpointUrl}"
                }
            }
            override fun onFailure(call: Call<SchoolIntegrationResponse>, t: Throwable) {}
        })
    }

    private fun updateUserProfile(profilePicture: String? = null, identitas: String? = null, namaLengkap: String? = null, password: String? = null) {
        if (currentUserId == -1) return
        
        val request = ProfileUpdateRequest(profilePicture, identitas, namaLengkap, password)
        NetworkClient.apiService.updateUserProfile(currentUserId, request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                    loadUserProfile() // reload UI
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Koneksi Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditIdentitasDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_identitas, null)
        bottomSheetDialog.setContentView(dialogView)

        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_name)
        val etSchool = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_school)
        val etNim = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_nim)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_identitas)

        // Pre-fill
        etName.setText(binding.tvProfileName.text)
        currentIdentitasRaw?.let {
            try {
                val jsonObj = JSONObject(it)
                etSchool.setText(jsonObj.optString("sekolah", ""))
                etNim.setText(jsonObj.optString("nim", ""))
            } catch(e: Exception) {
                etSchool.setText(it)
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val school = etSchool.text.toString()
            val nim = etNim.text.toString()

            if (name.isNotEmpty() && school.isNotEmpty()) {
                val json = JSONObject()
                json.put("sekolah", school)
                json.put("nim", nim)
                
                updateUserProfile(namaLengkap = name, identitas = json.toString())
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Nama dan Sekolah wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }
    
    private fun showChangePasswordDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        bottomSheetDialog.setContentView(dialogView)

        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_new_password)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_password)

        btnSave.setOnClickListener {
            val password = etNewPassword.text.toString()
            if (password.length >= 6) {
                updateUserProfile(password = password)
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Kata sandi minimal 6 karakter", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }

    private fun showSetupApiDialog(userId: Int) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_school_api, null)
        bottomSheetDialog.setContentView(dialogView)

        val etUrl = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_api_url)
        val etToken = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_api_token)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_api)

        btnSave.setOnClickListener {
            val url = etUrl.text.toString()
            val token = etToken.text.toString()

            if (url.isNotEmpty() && userId != -1) {
                val request = SchoolIntegrationRequest(userId, url, token)
                NetworkClient.apiService.updateSchoolIntegration(request).enqueue(object : Callback<SchoolIntegrationResponse> {
                    override fun onResponse(call: Call<SchoolIntegrationResponse>, response: Response<SchoolIntegrationResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Integrasi disimpan", Toast.LENGTH_SHORT).show()
                            binding.tvApiInfo.text = "Terhubung ke: $url"
                            bottomSheetDialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<SchoolIntegrationResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "URL tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }
    
    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val maxImageSize = 500 // compress to 500px width/height max to prevent huge payload
        val ratio: Float = Math.min(
            maxImageSize.toFloat() / bitmap.width,
            maxImageSize.toFloat() / bitmap.height
        )
        val width = Math.round(ratio * bitmap.width)
        val height = Math.round(ratio * bitmap.height)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        
        val baos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
