package com.example.movielife.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.PostAdapter
import com.example.movielife.PostPelicula
import com.example.movielife.R
import com.example.movielife.User
import com.example.movielife.databinding.FragmentHomeBinding
import com.example.movielife.databinding.FragmentSeguidosHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SeguidosHomeFragment : Fragment() {

    private lateinit var binding: FragmentSeguidosHomeBinding
    private val postList = mutableListOf<PostPelicula>()
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

    private fun cargarPostsSeguidos() {
        val database = FirebaseDatabase.getInstance()
        val seguidoresRef = database.getReference("seguidores").child(uid)

        seguidoresRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followedUids = mutableSetOf<String>()
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

                cargarPostsDeUsuariosSeguidos(followedUids)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeguidosLog", "Error cargando seguidores: ${error.message}")
            }
        })
    }

    private fun cargarPostsDeUsuariosSeguidos(followedUids: Set<String>) {
        val postsRef = FirebaseDatabase.getInstance().getReference("postspeliculas")

        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredPosts = mutableListOf<PostPelicula>()
                val uidsInPosts = mutableSetOf<String>()

                for (child in snapshot.children) {
                    val post = child.getValue(PostPelicula::class.java)
                    if (post != null && followedUids.contains(post.uid)) {
                        filteredPosts.add(post)
                        uidsInPosts.add(post.uid)
                    }
                }

                val sortedPosts = filteredPosts.sortedByDescending { it.timestamp }
                fetchUsersAndSetAdapter(sortedPosts, uidsInPosts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeguidosLog", "Error cargando posts: ${error.message}")
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
                    Log.e("SeguidosLog", "Error cargando usuario: ${error.message}")
                }
            })
        }
    }
}
