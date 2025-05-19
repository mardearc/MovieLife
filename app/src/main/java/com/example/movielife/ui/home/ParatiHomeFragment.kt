package com.example.movielife.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.PostAdapter
import com.example.movielife.PostPelicula
import com.example.movielife.User
import com.example.movielife.databinding.FragmentParatiHomeBinding
import com.google.firebase.database.*

class ParatiHomeFragment : Fragment() {

    private lateinit var binding: FragmentParatiHomeBinding
    private val postList = mutableListOf<PostPelicula>()
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
                        Log.d("ParatiPostLog", "Post: ${post.comentario} - ${post.timestamp}")
                    }
                }

                if (postList.isEmpty()) {
                    Log.d("ParatiPostLog", "No se encontraron posts")
                    binding.recyclerViewPosts.adapter = PostAdapter(emptyList(), emptyMap())
                    return
                }

                val postListOrdenado = postList.sortedByDescending { it.timestamp }
                fetchUsersAndSetAdapter(postListOrdenado, uidSet)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ParatiPostLog", "Error cargando posts: ${error.message}")
            }
        })
    }

    private fun fetchUsersAndSetAdapter(postList: List<PostPelicula>, uidSet: Set<String>) {
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
