package com.example.movielife.ui.profile

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
import com.example.movielife.databinding.FragmentProfilePostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilePostFragment : Fragment() {

    private lateinit var uid: String
    private lateinit var binding: FragmentProfilePostBinding
    private val postList = mutableListOf<Post>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostAdapter

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
        adapter = PostAdapter(postList, userMap)
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


    // Cargar post del usuario que se visualiza
    private fun cargarPostsDeUsuario(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userPostsRef = database.getReference("usuarios").child(uid).child("postspeliculas")
        val postsRef = database.getReference("postspeliculas")

        userPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postIds = mutableListOf<String>()
                // Buscar todos los posts
                for (child in snapshot.children) {
                    postIds.add(child.key!!)
                }

                if (postIds.isEmpty()) {
                    Log.d("ProfilePostLog", "El usuario no tiene posts")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                val postList = mutableListOf<Post>()
                val userMap = mutableMapOf<String, User>()
                var fetchedPosts = 0

                // Recuperar información de cada post
                for (postId in postIds) {
                    postsRef.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(postSnapshot: DataSnapshot) {
                            val post = postSnapshot.getValue(Post::class.java)
                            if (post != null) {
                                postList.add(post)
                            }

                            fetchedPosts++
                            if (fetchedPosts == postIds.size) {
                                val postListOrdenado = postList.sortedByDescending { it.timestamp }
                                // Recuperamos los datos del usuario
                                FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                userMap[uid] = user
                                            }
                                            binding.recyclerViewPosts.adapter =
                                                PostAdapter(postListOrdenado, userMap)
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