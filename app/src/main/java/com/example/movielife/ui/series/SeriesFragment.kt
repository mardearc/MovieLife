package com.example.movielife.ui.series

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movielife.model.ApiService
import com.example.movielife.ui.series.DetailSerieActivity.Companion.EXTRA_ID
import com.example.movielife.R
import com.example.movielife.ui.adapters.SerieAdapter
import com.example.movielife.databinding.FragmentSeriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SeriesFragment : Fragment() {
    private lateinit var binding: FragmentSeriesBinding

    private lateinit var retrofit: Retrofit
    private lateinit var adapter: SerieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSeriesBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        retrofit = getRetrofit()
        initUI()

        return binding.root


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_busqueda, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Buscar película..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Realizar la búsqueda
                query?.let { searchByName(it) }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        searchByName(query)
                    }else{
                        popularSeries()
                    }
                }
                return true
            }
        })
    }

    private fun initUI() {
        adapter = SerieAdapter { navigateToDetail(it) }
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
        popularSeries()

    }

    private fun popularSeries() {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        val language = "es-ES"
        val page = 1

        binding.tvBusqueda.setText(getString(R.string.popular_esta_semana))
        binding.pbBuscador.isVisible = true

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val apiService = retrofit.create(ApiService::class.java)
                val myResponse = apiService.getPopularSeries(apiKey, language, page)

                val peliculas = myResponse.body()
                if (peliculas != null) {
                    requireActivity().runOnUiThread {
                        adapter.updateList(peliculas.peliculas)
                        binding.pbBuscador.isVisible = false

                    }
                }
            }
        } catch (e: Exception) {
            Log.e("popularPeliculas", "Error: ${e.message}")
            requireActivity().runOnUiThread {
                binding.pbBuscador.isVisible = false
            }
        }
    }

    private fun searchByName(query: String) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        val language = "es-ES"
        val page = 1
        val includeAdult = false

        binding.tvBusqueda.setText(buildString {
            append(getString(R.string.resultados_para))
            append(query)
        })
        binding.pbBuscador.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = retrofit.create(ApiService::class.java)
                val myResponse = apiService.getSeries(
                    apiKey = apiKey,
                    language = language,
                    peliculaName = query,
                    page = page,
                    includeAdult = includeAdult
                )

                if (myResponse.isSuccessful) {
                    val peliculas =
                        myResponse.body() // Aquí tienes acceso a los datos de la respuesta
                    if (peliculas != null) {
                        Log.i("mardearc", peliculas.toString())
                        requireActivity().runOnUiThread {
                            adapter.updateList(peliculas.peliculas)
                            binding.pbBuscador.isVisible = false
                        }

                    }
                    Log.i("mardearc", "Funciona: $peliculas")
                } else {
                    Log.e("mardearc", "Error: ${myResponse.code()} - ${myResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e("mardearc", "Error de red: ${e.message}")
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
        val intent = Intent(requireContext(), DetailSerieActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }
}
