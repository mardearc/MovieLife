package com.example.movielife.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.movielife.ProfilePagerAdapter
import com.example.movielife.R
import com.example.movielife.User
import com.example.movielife.databinding.FragmentMoviesBinding
import com.example.movielife.databinding.FragmentProfileBinding
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
        setupFollowButton()
        loadUserInfo()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewedUserId = arguments?.getString("uid") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }


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

    private fun countMedia() {
        val db = FirebaseDatabase.getInstance()

        val userRef = db.getReference("usuarios").child(viewedUserId)

        userRef.child("peliculasVistas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val peliculasCount = snapshot.childrenCount
                binding.tvPeliculasCount.text = "$peliculasCount"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("seriesVistas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val seriesCount = snapshot.childrenCount
                binding.tvSeriesCount.text = "$seriesCount"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun setupFollowButton() {
        if (viewedUserId == currentUserId) {
            binding.btnSeguir.visibility = View.GONE
            return
        }

        val followRef = FirebaseDatabase.getInstance().getReference("seguidores").child(currentUserId)

        followRef.child(viewedUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sigue = snapshot.exists()
                updateFollowButton(sigue)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.btnSeguir.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("seguidores").child(currentUserId)

            ref.child(viewedUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        ref.child(viewedUserId).removeValue()
                        updateFollowButton(false)
                    } else {
                        ref.child(viewedUserId).setValue(true)
                        updateFollowButton(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun updateFollowButton(sigue: Boolean) {
        binding.btnSeguir.text = if (sigue) "Siguiendo" else "Seguir"
    }



}