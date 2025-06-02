package com.example.movielife.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.ui.adapters.PostAdapter
import com.example.movielife.model.Post
import com.example.movielife.model.User
import com.example.movielife.databinding.FragmentSeguidosHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SeguidosHomeFragment : Fragment() {

    private lateinit var binding: FragmentSeguidosHomeBinding
    private val postList = mutableListOf<Post>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostAdapter
    private lateinit var uid: String

    companion object {
        fun newInstance(uid: String): SeguidosHomeFragment {
            val fragment = SeguidosHomeFragment()
            val args = Bundle()
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeguidosHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PostAdapter(postList, userMap)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = adapter

        cargarPostsSeguidos()
    }

    // Cargar todos los posts de usuarios seguidos(series y películas)
    private fun cargarPostsSeguidos() {
        val database = FirebaseDatabase.getInstance()
        val seguidoresRef = database.getReference("seguidores").child(uid)

        seguidoresRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followedUids = mutableSetOf<String>()
                // Recorrer todos los usuarios y añadir la a la lista los seguidos
                for (child in snapshot.children) {
                    if (child.getValue(Boolean::class.java) == true) {
                        followedUids.add(child.key ?: "")
                    }
                }

                if (followedUids.isEmpty()) {
                    Log.d("SeguidosLog", "El usuario no sigue a nadie.")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                cargarPostsGlobales(followedUids)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeguidosLog", "Error cargando seguidores: ${error.message}")
            }
        })
    }

    // Cargar posts de seguidos
    private fun cargarPostsGlobales(followedUids: Set<String>) {
        val database = FirebaseDatabase.getInstance()
        val postsPeliculasRef = database.getReference("postspeliculas")
        val postsSeriesRef = database.getReference("postsseries")

        val posts = mutableListOf<Post>()
        val uidSet = mutableSetOf<String>()

        val postsCargados = mutableListOf<Boolean>()

        // Verficar si se han cargado todos los posts
        fun verificarCargaCompleta() {
            if (postsCargados.size >= 1) {
                // Si no hay post se inicia adapter vacío
                if (posts.isEmpty()) {
                    Log.d("ParatiPostLog", "No se encontraron posts")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                // Ordenar post por fecha
                val postListOrdenado = posts.sortedByDescending { it.timestamp }
                fetchUsersAndSetAdapter(postListOrdenado, uidSet)
            }
        }

        // Cargar posts de películas
        postsPeliculasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val post = child.getValue(Post::class.java)
                    if (post != null && followedUids.contains(post.uid)) {
                        posts.add(post)
                        uidSet.add(post.uid)
                    }
                }
                postsCargados.add(true)
                verificarCargaCompleta()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ParatiPostLog", "Error cargando posts de películas: ${error.message}")
                postsCargados.add(true)
                verificarCargaCompleta()
            }
        })

        // Cargar posts de series
        postsSeriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val post = child.getValue(Post::class.java)
                    if (post != null  && followedUids.contains(post.uid)) {
                        posts.add(post)
                        uidSet.add(post.uid)
                    }
                }
                postsCargados.add(true)
                verificarCargaCompleta()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ParatiPostLog", "Error cargando posts de series: ${error.message}")
                postsCargados.add(true)
                verificarCargaCompleta()
            }
        })
    }

    // Obtener datos de los usuarios
    private fun fetchUsersAndSetAdapter(postList: List<Post>, uidSet: Set<String>) {
        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios")
        val userMap = mutableMapOf<String, User>()
        var fetchedUsers = 0

        // Si no hay usuarios se asigna el adapter sin usuarios
        if (uidSet.isEmpty()) {
            binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
            return
        }

        // Buscar informacion de cada usuario
        for (uid in uidSet) {
            usuariosRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        userMap[uid] = user
                    }

                    fetchedUsers++
                    // Cargar adapter con todos los usuarios
                    if (fetchedUsers == uidSet.size) {
                        binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SeguidosLog", "Error cargando usuario: ${error.message}")
                }
            })
        }
    }
}
