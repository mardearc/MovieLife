package com.example.movielife.ui.profile

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
import com.example.movielife.databinding.FragmentProfileHistorialBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileHistorialFragment : Fragment(){

    private lateinit var uid: String
    private lateinit var binding:FragmentProfileHistorialBinding
    private val postList = mutableListOf<PostPelicula>()
    private val userMap = mutableMapOf<String, User>()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PostAdapter(postList, userMap)
        binding.recyclerViewHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistorial.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid") ?: ""
    }

    companion object {
        fun newInstance(uid: String): ProfileHistorialFragment {
            val fragment = ProfileHistorialFragment()
            val args = Bundle().apply { putString("uid", uid) }
            fragment.arguments = args
            return fragment
        }
    }


}