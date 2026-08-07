package com.example.contactappxml

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditContactActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private var imageUri: Uri? = null
    private val supabase = SupabaseManager.client // Manager ka use kiya

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addedit_contact)

        profileImage = findViewById(R.id.profileImage)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnSave = findViewById<Button>(R.id.btnSave)

        profileImage.setOnClickListener { getImage.launch("image/*") }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            // 1. Name Validation
            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }

            // 2. Phone Number Validation (exactly 10 digits)
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                etPhone.error = "Enter a valid 10-digit mobile number"
                return@setOnClickListener
            }

            // 3. Email Validation (if not empty, check format)
            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }

            // Agar sab sahi hai tabhi save karein
            saveData(name, phone, email)
        }
    }

    private fun saveData(name: String, phone: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var imageUrl: String? = null
                
                if (imageUri != null) {
                    val bytes = contentResolver.openInputStream(imageUri!!)?.readBytes()
                    if (bytes != null) {
                        val fileName = "photo_${System.currentTimeMillis()}.jpg"
                        val bucket = supabase.storage.from("contact_photos")
                        bucket.upload(fileName, bytes)
                        imageUrl = bucket.publicUrl(fileName)
                    }
                }

                val contact = Contact(
                    name = name,
                    phone = if (phone.isEmpty()) null else phone,
                    email = if (email.isEmpty()) null else email,
                    image_url = imageUrl
                )
                
                supabase.from("contacts").insert(contact)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddEditContactActivity, "Contact Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddEditContactActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
