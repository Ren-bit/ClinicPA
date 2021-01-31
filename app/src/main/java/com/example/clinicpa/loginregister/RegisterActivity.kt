package com.example.clinicpa.loginregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.clinicpa.BerandaActivity
import com.example.clinicpa.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btnRegis.setOnClickListener {
            val email = etEmailReg.text.toString().trim()
            val password = etPasswordReg.text.toString().trim()
            if (email.isEmpty()) {
                etEmailReg.error = "Email Harus Di Isi"
                etEmailReg.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailReg.error = "Email Tidak Valid"
                etEmailReg.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                etPasswordReg.error = "Password Minimal 6 Karakter"
            }
            registerUser(email, password)
        }

        btnBack.setOnClickListener {
            Intent(this@RegisterActivity, LoginActivity::class.java).also {
                startActivity(it)
            }

        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Intent(this@RegisterActivity, BerandaActivity::class.java).also { intent ->
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            Intent(this@RegisterActivity, BerandaActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}