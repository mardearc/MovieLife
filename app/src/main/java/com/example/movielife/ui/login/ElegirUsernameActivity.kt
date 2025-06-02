package com.example.movielife.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.movielife.MainActivity
import com.example.movielife.databinding.ActivityElegirUsernameBinding
import com.example.movielife.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ElegirUsernameActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var binding : ActivityElegirUsernameBinding
    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityElegirUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var fotoSeleccionada = "imgperfil1"

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvFinalUserName.text = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        // Asignar la imagen que el usuario pulse
        binding.cardview1.setOnClickListener {
            fotoSeleccionada = "imgperfil1"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }

        binding.cardview2.setOnClickListener {
            fotoSeleccionada = "imgperfil2"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }

        binding.cardview3.setOnClickListener {
            fotoSeleccionada = "imgperfil3"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }

        binding.cardview4.setOnClickListener {
            fotoSeleccionada = "imgperfil4"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }

        binding.cardview5.setOnClickListener {
            fotoSeleccionada = "imgperfil5"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }

        binding.cardview6.setOnClickListener {
            fotoSeleccionada = "imgperfil6"
            val resId = resources.getIdentifier(fotoSeleccionada, "drawable", packageName)
            binding.profilePicture.setImageResource(resId)
        }


        // Comprobaciones al pulsar en continuar
        binding.btnContinuar.setOnClickListener {
            val nombreUsuario = binding.editTextUsername.text.toString().trim()

            if (nombreUsuario.length < 4) {
                binding.editTextUsername.error = "Mínimo 4 caracteres"
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().reference


            database.child("usuarios").get().addOnSuccessListener { snapshot ->
                var nombreYaExiste = false

                for (usuarioSnapshot in snapshot.children) {
                    val usuarioExistente = usuarioSnapshot.child("nombreUsuario").value as? String
                    if (usuarioExistente != null && usuarioExistente.equals(nombreUsuario, ignoreCase = true)) {
                        nombreYaExiste = true
                        break
                    }
                }

                if (nombreYaExiste) {
                    binding.editTextUsername.error = "Este nombre de usuario ya está en uso"
                    return@addOnSuccessListener
                }

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val usuario = User(
                    uid = uid,
                    nombreUsuario = nombreUsuario,
                    fotoPerfil = fotoSeleccionada
                )

                // Registrar usuario en Firebase
                database.child("usuarios")
                    .child(uid)
                    .setValue(usuario)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

            }
        }

    }
}