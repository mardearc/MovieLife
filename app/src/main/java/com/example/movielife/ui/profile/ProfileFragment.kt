package com.example.movielife.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.example.movielife.ui.login.LoginActivity
import com.example.movielife.R
import com.example.movielife.model.User
import com.example.movielife.databinding.FragmentProfileBinding
import com.example.movielife.ui.adapters.ProfilePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding

    private lateinit var viewedUserId: String
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        val adapter = ProfilePagerAdapter(this, currentUserId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Comentarios"
                1 -> "Historial"
                else -> null
            }
        }.attach()

        countMedia()
        loadUserInfo()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewedUserId = arguments?.getString("uid") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        setHasOptionsMenu(true)
    }

    // Cargar info usuarios
    private fun loadUserInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(viewedUserId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.tvUsername.text = "@${it.nombreUsuario}"

                    // Imagen de perfil
                    val context = requireContext()
                    val imgId = context.resources.getIdentifier(it.fotoPerfil, "drawable", context.packageName)
                    binding.imgPerfil.setImageResource(if (imgId != 0) imgId else R.drawable.ic_launcher_foreground)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Contar número de películas y series vistas
    private fun countMedia() {
        val db = FirebaseDatabase.getInstance()

        val userRef = db.getReference("usuarios").child(viewedUserId)

        // Número de pelícualas
        userRef.child("peliculasVistas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val peliculasCount = snapshot.childrenCount
                binding.tvPeliculasCount.text = "$peliculasCount"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Número de series
        userRef.child("seriesVistas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val seriesCount = snapshot.childrenCount
                binding.tvSeriesCount.text = "$seriesCount"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cerrar_sesion -> {
                mostrarDialogoCerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Cerrar sesión
    private fun mostrarDialogoCerrarSesion() {
        val dialog = AlertDialog.Builder(requireContext()).create()
        dialog.setTitle("Cerrar sesión")
        dialog.setMessage("¿Seguro que quieres cerrar sesión?")

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { _, _ ->
            dialog.dismiss()
        }

        // Si es que si se cierra la sesión de ese usuario, eliminando datos residuales
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { _, _ ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        dialog.show()
    }
}