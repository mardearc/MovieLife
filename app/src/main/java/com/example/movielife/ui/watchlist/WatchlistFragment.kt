package com.example.movielife.ui.watchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movielife.ApiService
import com.example.movielife.DetailPeliculaActivity
import com.example.movielife.DetailPeliculaActivity.Companion.EXTRA_ID
import com.example.movielife.PeliculaAdapter
import com.example.movielife.PeliculaItemResponse
import com.example.movielife.R
import com.example.movielife.databinding.FragmentMoviesBinding
import com.example.movielife.databinding.FragmentWatchlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WatchlistFragment : Fragment() {
    private lateinit var binding: FragmentWatchlistBinding

    private lateinit var retrofit: Retrofit
    private lateinit var adapter: PeliculaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWatchlistBinding.inflate(layoutInflater)

        retrofit = getRetrofit()
        initUI()

        return binding.root


    }

    private fun initUI() {
        adapter = PeliculaAdapter { navigateToDetail(it) }
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerView.adapter = adapter
        searchWatchlist()
    }

    private fun searchWatchlist() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

        binding.pbBuscador.isVisible = true
        database.child("watchlistPeliculas").get().addOnSuccessListener { snapshot ->
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