package com.group2.intercom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginUserIdActivity : AppCompatActivity() {

    private lateinit var edtUserId: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnSubmit: Button
    private lateinit var txtUserIdError: TextView
    private lateinit var txtPasswordError: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_login_userid)

        edtUserId = findViewById(R.id.edtUserId)
        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnSubmit = findViewById(R.id.btnSubmit)
        txtUserIdError = findViewById(R.id.txtUserIdError)
        txtPasswordError = findViewById(R.id.txtPasswordError)

        edtUserId.addTextChangedListener(createTextWatcher(txtUserIdError))
        edtConfirmPassword.addTextChangedListener(createTextWatcher(txtPasswordError))

        btnSubmit.setOnClickListener {
            val userId = edtUserId.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if (userId.isEmpty()) {
                txtUserIdError.text = "User ID cannot be empty"
                txtUserIdError.visibility = View.VISIBLE
            } else if (password.isEmpty() || confirmPassword.isEmpty()) {
                txtPasswordError.text = "Password fields cannot be empty"
                txtPasswordError.visibility = View.VISIBLE
            } else if (password != confirmPassword) {
                txtPasswordError.text = "Passwords do not match"
                txtPasswordError.visibility = View.VISIBLE
            } else {
                checkUserIdAvailability(userId, password)
            }
        }
    }

    private fun checkUserIdAvailability(userId: String, password: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    txtUserIdError.text = "This User ID is already taken. Please choose another."
                    txtUserIdError.visibility = View.VISIBLE
                } else {
                    val email = "$userId@gmail.com"
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val userData = hashMapOf(
                                "userId" to userId,
                                "createdAt" to System.currentTimeMillis()
                            )

                            firestore.collection("users").document(userId).set(userData)
                                .addOnSuccessListener {
                                    // Save userId to SharedPreferences
                                    getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("userId", userId)
                                        .apply()

                                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this, DeviceInfo::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FIRESTORE", "Failed to register user", e)
                                    Toast.makeText(this, "Failed to save user. Try again.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AUTH", "Firebase Auth failed", e)
                            Toast.makeText(this, "Auth failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Failed to check user ID", e)
                Toast.makeText(this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createTextWatcher(errorTextView: TextView) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            errorTextView.visibility = View.GONE
        }
        override fun afterTextChanged(s: Editable?) {}
    }
}
