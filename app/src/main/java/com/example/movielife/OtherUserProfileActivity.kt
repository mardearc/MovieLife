package com.example.movielife

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.movielife.databinding.ActivityOtherUserProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OtherUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherUserProfileBinding

    private lateinit var viewedUserId: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewedUserId = intent.getStringExtra("uid") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupViewPagerAndTabs()
        loadUserInfo()
        countMedia()
        setupFollowButton()
    }

    private fun setupViewPagerAndTabs() {
        val adapter = ProfilePagerAdapterActivity(this, viewedUserId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Comentarios"
                1 -> "Historial"
                else -> null
            }
        }.attach()
    }

    private fun loadUserInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(viewedUserId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.tvUsername.text = "@${it.nombreUsuario}"

                    val context = applicationContext
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
        // Si es el mismo usuario, no se muestra el botón de seguir
        if (viewedUserId == currentUserId) {
            binding.btnSeguir.visibility = View.GONE
            return
        }else{
            binding.btnSeguir.visibility = View.VISIBLE
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

        if(!sigue){
            binding.btnSeguir.setBackgroundColor(ContextCompat.getColor(this, R.color.green_principal))
            binding.btnSeguir.textSize = 14F
        }else{
            binding.btnSeguir.setBackgroundColor(ContextCompat.getColor(this, R.color.green_secondary_prov))
            binding.btnSeguir.textSize = 10F
        }
    }
}
