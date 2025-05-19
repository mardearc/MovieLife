package com.example.movielife.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.PostPeliculaAdapter
import com.example.movielife.PostPelicula
import com.example.movielife.User
import com.example.movielife.databinding.FragmentProfilePostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilePostFragment : Fragment() {

    private lateinit var uid: String
    private lateinit var binding: FragmentProfilePostBinding
    private val postList = mutableListOf<PostPelicula>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostPeliculaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfilePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid") ?: ""
        cargarPostsDeUsuario(uid)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PostPeliculaAdapter(postList, userMap)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = adapter
    }

    companion object {
        fun newInstance(uid: String): ProfilePostFragment {
            val fragment = ProfilePostFragment()
            val args = Bundle().apply { putString("uid", uid) }
            fragment.arguments = args
            return fragment
        }
    }


    private fun cargarPostsDeUsuario(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userPostsRef = database.getReference("usuarios").child(uid).child("postspeliculas")
        val postsRef = database.getReference("postspeliculas")

        userPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postIds = mutableListOf<String>()
                for (child in snapshot.children) {
                    postIds.add(child.key!!)
                }

                if (postIds.isEmpty()) {
                    Log.d("ProfilePostLog", "El usuario no tiene posts")
                    binding.recyclerViewPosts.adapter = PostPeliculaAdapter(emptyList(), emptyMap())
                    return
                }

                val postList = mutableListOf<PostPelicula>()
                val userMap = mutableMapOf<String, User>()
                var fetchedPosts = 0

                for (postId in postIds) {
                    postsRef.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(postSnapshot: DataSnapshot) {
                            val post = postSnapshot.getValue(PostPelicula::class.java)
                            if (post != null) {
                                postList.add(post)
                            }

                            fetchedPosts++
                            if (fetchedPosts == postIds.size) {
                                // Una vez que se hayan recuperado todos los posts
                                val postListOrdenado = postList.sortedByDescending { it.timestamp }
                                // Recuperamos los datos del usuario (ya que solo es uno)
                                FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                userMap[uid] = user
                                            }
                                            binding.recyclerViewPosts.adapter =
                                                PostPeliculaAdapter(postListOrdenado, userMap)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("ProfilePostLog", "Error al obtener usuario: ${error.message}")
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("ProfilePostLog", "Error al obtener post: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfilePostLog", "Error al obtener posts del usuario: ${error.message}")
            }
        })
    }

}