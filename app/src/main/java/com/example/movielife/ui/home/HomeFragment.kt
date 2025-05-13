package com.example.movielife.ui.home

import com.example.movielife.User
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.PostAdapter
import com.example.movielife.PostPelicula
import com.example.movielife.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val postList = mutableListOf<PostPelicula>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PostAdapter(postList, userMap)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = adapter

        cargarPostsGlobales()
    }

    private fun cargarPostsGlobales() {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.getReference("postspeliculas")

        val postList = mutableListOf<PostPelicula>()
        val uidSet = mutableSetOf<String>()

        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val post = child.getValue(PostPelicula::class.java)
                    if (post != null) {
                        postList.add(post)
                        uidSet.add(post.uid)
                        Log.d("HomePostLog", "Post: ${post.comentario} - ${post.timestamp}")
                    }
                }

                if (postList.isEmpty()) {
                    Log.d("HomePostLog", "No se encontraron posts globales")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                // Ordenar por timestamp descendente
                val postListOrdenado = postList.sortedByDescending { it.timestamp }

                fetchUsersAndSetAdapter(postListOrdenado, uidSet)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomePostLog", "Error cargando posts: ${error.message}")
            }
        })
    }

    private fun fetchUsersAndSetAdapter(postList: List<PostPelicula>, uidSet: Set<String>) {
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("usuarios")
        val userMap = mutableMapOf<String, User>()
        var fetchedUsers = 0

        if (uidSet.isEmpty()) {
            Log.d("PostLog", "No hay usuarios a recuperar")
            binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
            return
        }

        for (uid in uidSet) {
            Log.d("PostLog", "Recuperando datos del usuario: $uid")
            usuariosRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        userMap[uid] = user
                        Log.d("PostLog", "Usuario recuperado: ${user.nombreUsuario}")
                    } else {
                        Log.d("PostLog", "Usuario nulo para UID: $uid")
                    }

                    fetchedUsers++
                    if (fetchedUsers == uidSet.size) {
                        Log.d("PostLog", "Usuarios recuperados: ${userMap.size}")
                        binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PostLog", "Error recuperando usuario: ${error.message}")
                }
            })
        }
    }

}

