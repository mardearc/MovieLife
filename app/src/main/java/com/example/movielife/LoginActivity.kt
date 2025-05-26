package com.example.movielife

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("GoogleSignIn", "Cuenta de Google obtenida: ${account?.email}")
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser

                                if (user != null) {
                                    val uid = user.uid
                                    val ref = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

                                    ref.get().addOnSuccessListener { snapshot ->
                                        if (!snapshot.exists()) {
                                            // Usuario completamente nuevo: creamos su nodo con email, etc.
                                            val nuevoUsuario = mapOf(
                                                "email" to user.email,
                                                "fotoPerfil" to user.photoUrl?.toString(),
                                                "nombreUsuario" to null
                                            )
                                            ref.setValue(nuevoUsuario).addOnCompleteListener {
                                                startActivity(Intent(this, ElegirUsernameActivity::class.java))
                                                finish()
                                            }
                                        } else {
                                            // Usuario ya existe en la base de datos
                                            val nombreUsuario = snapshot.child("nombreUsuario").value
                                            if (nombreUsuario == null || nombreUsuario.toString().isBlank()) {
                                                startActivity(Intent(this, ElegirUsernameActivity::class.java))
                                                finish()
                                            } else {
                                                goToMain()
                                            }
                                        }
                                    }.addOnFailureListener {
                                        Log.w("GoogleSignIn", "Error al comprobar datos del usuario.")
                                        Toast.makeText(this, "Error al comprobar datos del usuario", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            } else {
                                Log.w("GoogleSignIn", "Error de autenticación con Firebase.")
                                Toast.makeText(this, "Fallo con Google", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: ApiException) {
                    Log.w("GoogleSignIn", "Fallo en el inicio de sesión de Google: ${e.statusCode}")
                    Toast.makeText(this, "Fallo en el inicio de sesión de Google", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("GoogleSignIn", "Resultado no OK del inicio de sesión.")
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser != null) {
            goToMain()
            return
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val emailBox = findViewById<EditText>(R.id.emailInput)
            val pass1 = findViewById<EditText>(R.id.passwordInput)

            val email = emailBox.text.toString()
            val pass = pass1.text.toString()

            if (emailBox.text.isEmpty()) {
                emailBox.error = "Correo requerido"
                return@setOnClickListener
            }

            if (pass1.text.isEmpty()) {
                pass1.error = "Contraseña requerida"
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if (it.isSuccessful) goToMain()
                    else Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }

        findViewById<TextView>(R.id.goToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }

    private fun goToMain() {
        Log.d("LoginActivity", "Redirigiendo a MainActivity.")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
