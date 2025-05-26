package com.example.movielife.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movielife.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnRegist).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailInput)
            val pass1 = findViewById<EditText>(R.id.passwordInput)
            val pass2 = findViewById<EditText>(R.id.passwordInput2)

            if (email.text.isEmpty()) {
                email.error = "Correo requerido"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                email.error = "Correo no válido"
                return@setOnClickListener
            }

            if (pass1.text.isEmpty()) {
                pass1.error = "Contraseña requerida"
                return@setOnClickListener
            }

            if (pass1.text.length < 8) {
                pass1.error = "Mínimo 8 caracteres"
                return@setOnClickListener
            }

            if (pass1.text.toString() != pass2.text.toString()) {
                pass2.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email.text.toString(), pass1.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, ElegirUsernameActivity::class.java))
                        finish()
                    } else {
                        val mensaje = it.exception?.localizedMessage ?: "Error desconocido al registrar"
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        Log.e("RegisterActivity", "Error al registrar", it.exception)
                    } }

            }

        findViewById<TextView>(R.id.goToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }
}