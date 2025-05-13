package com.example.movielife.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.ApiService
import com.example.movielife.DetailPeliculaActivity
import com.example.movielife.DetailPeliculaActivity.Companion.EXTRA_ID
import com.example.movielife.PeliculaAdapter
import com.example.movielife.PeliculaItemResponse
import com.example.movielife.PostAdapter
import com.example.movielife.PostPelicula
import com.example.movielife.R
import com.example.movielife.User
import com.example.movielife.databinding.FragmentHomeBinding
import com.example.movielife.databinding.FragmentProfileHistorialBinding
import com.example.movielife.databinding.FragmentWatchlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileHistorialFragment : Fragment(){

    private lateinit var uid: String
    private lateinit var binding:FragmentProfileHistorialBinding

    private lateinit var retrofit: Retrofit
    private lateinit var adapter: PeliculaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileHistorialBinding.inflate(inflater, container, false)

        retrofit = getRetrofit()
        initUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PeliculaAdapter { navigateToDetail(it) }
        binding.recyclerViewHistorial.setHasFixedSize(true)
        binding.recyclerViewHistorial.layoutManager = GridLayoutManager(requireContext(), 4)
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



    private fun initUI() {
        adapter = PeliculaAdapter { navigateToDetail(it) }
        binding.recyclerViewHistorial.setHasFixedSize(true)
        binding.recyclerViewHistorial.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerViewHistorial.adapter = adapter
        searchPeliculasVistas()
    }

    private fun searchPeliculasVistas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

        binding.pbBuscador.isVisible = true
        database.child("peliculasVistas").get().addOnSuccessListener { snapshot ->
            val idList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
            if (idList.isNotEmpty()) {
                getPeliculaData(idList)
            } else {
                requireActivity().runOnUiThread {
                    adapter.updateList(emptyList())
                    binding.pbBuscador.isVisible = false
                }
            }
        }
    }

    private fun getPeliculaData(ids: List<String>) {
        val apiService = retrofit.create(ApiService::class.java)
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        val language = "es-ES"

        val peliculasList = mutableListOf<PeliculaItemResponse>()

        CoroutineScope(Dispatchers.IO).launch {
            for (id in ids) {
                try {
                    val response = apiService.getMovieByIdToList(id.toInt(), apiKey, language)
                    if (response.isSuccessful) {
                        response.body()?.let { pelicula ->
                            peliculasList.add(pelicula)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("mardearc", "Error cargando ID $id: ${e.message}")
                }
            }

            requireActivity().runOnUiThread {
                adapter.updateList(peliculasList)
                binding.pbBuscador.isVisible = false
            }
        }
    }



    private fun getRetrofit(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    private fun navigateToDetail(id: Int) {
        val intent = Intent(requireContext(), DetailPeliculaActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }


}