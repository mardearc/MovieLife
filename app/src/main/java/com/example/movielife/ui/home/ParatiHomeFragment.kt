package com.example.movielife.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.PostAdapter
import com.example.movielife.Post
import com.example.movielife.User
import com.example.movielife.databinding.FragmentParatiHomeBinding
import com.google.firebase.database.*

class ParatiHomeFragment : Fragment() {

    private lateinit var binding: FragmentParatiHomeBinding
    private val postList = mutableListOf<Post>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostAdapter

    companion object {
        fun newInstance(uid: String): ParatiHomeFragment {
            val fragment = ParatiHomeFragment()
            val args = Bundle()
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentParatiHomeBinding.inflate(inflater, container, false)
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
        val postsPeliculasRef = database.getReference("postspeliculas")
        val postsSeriesRef = database.getReference("postsseries")

        val todosLosPosts = mutableListOf<Post>() // Reutilizamos PostPelicula para ambos
        val uidSet = mutableSetOf<String>()

        val postsCargados = mutableListOf<Boolean>()

        fun verificarCargaCompleta() {
            if (postsCargados.size >= 1) {
                if (todosLosPosts.isEmpty()) {
                    Log.d("ParatiPostLog", "No se encontraron posts")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                val postListOrdenado = todosLosPosts.sortedByDescending { it.timestamp }
                fetchUsersAndSetAdapter(postListOrdenado, uidSet)
            }
        }

        // Cargar posts de películas
        postsPeliculasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val post = child.getValue(Post::class.java)
                    if (post != null) {
                        todosLosPosts.add(post)
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
                    val post = child.getValue(Post::class.java) // ⚠ Usa misma clase si es compatible
                    if (post != null) {
                        todosLosPosts.add(post)
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


    private fun fetchUsersAndSetAdapter(postList: List<Post>, uidSet: Set<String>) {
        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios")
        val userMap = mutableMapOf<String, User>()
        var fetchedUsers = 0

        if (uidSet.isEmpty()) {
            binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
            return
        }

        for (uid in uidSet) {
            usuariosRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        userMap[uid] = user
                    }

                    fetchedUsers++
                    if (fetchedUsers == uidSet.size) {
                        binding.recyclerViewPosts.adapter = PostAdapter(postList, userMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ParatiPostLog", "Error recuperando usuario: ${error.message}")
                }
            })
        }
    }
}
