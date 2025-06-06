package com.example.movielife.ui.watchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movielife.model.ApiService
import com.example.movielife.ui.movies.DetailPeliculaActivity
import com.example.movielife.ui.movies.DetailPeliculaActivity.Companion.EXTRA_ID
import com.example.movielife.ui.series.DetailSerieActivity
import com.example.movielife.ui.adapters.PeliculaAdapter
import com.example.movielife.model.PeliculaItemResponse
import com.example.movielife.ui.adapters.SerieAdapter
import com.example.movielife.model.SerieItemResponse
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
    private lateinit var peliculaAdapter: PeliculaAdapter
    private lateinit var serieAdapter: SerieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchlistBinding.inflate(layoutInflater)

        retrofit = getRetrofit()
        initUI()

        return binding.root


    }

    private fun initUI() {
        // Adapter de películas
        peliculaAdapter = PeliculaAdapter { navigateToDetail(it) }
        binding.recyclerViewHistorialPeliculas.setHasFixedSize(true)
        binding.recyclerViewHistorialPeliculas.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerViewHistorialPeliculas.adapter = peliculaAdapter

        // Adapter de series
        serieAdapter = SerieAdapter { navigateToDetailSerie(it) }
        binding.recyclerViewHistorialSeries.setHasFixedSize(true)
        binding.recyclerViewHistorialSeries.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerViewHistorialSeries.adapter = serieAdapter
        searchWatchlist()
        searchWatchlistSeries()
    }

    // Buscar watchlist de películas de un usuario
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
                    peliculaAdapter.updateList(emptyList())
                    binding.pbBuscador.isVisible = false
                }
            }
        }
    }

    // Buscar watchlist de películas de un usuario
    private fun searchWatchlistSeries() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

        binding.pbBuscador.isVisible = true
        database.child("watchlistSeries").get().addOnSuccessListener { snapshot ->
            val idList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
            if (idList.isNotEmpty()) {
                getSerieData(idList)
            } else {
                requireActivity().runOnUiThread {
                    serieAdapter.updateList(emptyList())
                    binding.pbBuscador.isVisible = false
                }
            }
        }
    }

    // Obtener información de películas y viajar a la activity
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
                peliculaAdapter.updateList(peliculasList)
                binding.pbBuscador.isVisible = false
            }
        }
    }

    // Obtener información de series y viajar a la activity
    private fun getSerieData(ids: List<String>) {
        val apiService = retrofit.create(ApiService::class.java)
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        val language = "es-ES"

        val seriesList = mutableListOf<SerieItemResponse>()

        CoroutineScope(Dispatchers.IO).launch {
            for (id in ids) {
                try {
                    val response = apiService.getSerieByIdItem(id.toInt(), apiKey, language)
                    if (response.isSuccessful) {
                        response.body()?.let { serie ->
                            seriesList.add(serie)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("mardearc", "Error cargando serie ID $id: ${e.message}")
                }
            }

            requireActivity().runOnUiThread {
                serieAdapter.updateList(seriesList)
                binding.pbBuscador2.isVisible = false
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

    // Navegar a DetailPeliculaActivity
    private fun navigateToDetail(id: Int) {
        val intent = Intent(requireContext(), DetailPeliculaActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }

    // Navegar a DetailSerieActivity
    private fun navigateToDetailSerie(id: Int) {
        val intent = Intent(requireContext(), DetailSerieActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }
}