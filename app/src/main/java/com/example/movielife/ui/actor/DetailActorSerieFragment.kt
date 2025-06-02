package com.example.movielife.ui.actor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movielife.R
import com.example.movielife.databinding.FragmentDetailActorSerieBinding
import com.example.movielife.model.ApiService
import com.example.movielife.model.SerieItemResponse
import com.example.movielife.ui.adapters.SerieAdapter
import com.example.movielife.ui.series.DetailSerieActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActorSerieFragment : Fragment() {

    private var actorId: Int = 0
    private lateinit var adapter: SerieAdapter
    private lateinit var binding: FragmentDetailActorSerieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actorId = arguments?.getInt("actor_id") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailActorSerieBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(actorId: Int, role: String): DetailActorSerieFragment {
            val fragment = DetailActorSerieFragment()
            fragment.arguments = Bundle().apply {
                putInt("actor_id", actorId)
                putString("role", role)
            }
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SerieAdapter { id ->
            val intent = Intent(requireContext(), DetailSerieActivity::class.java)
            intent.putExtra(DetailSerieActivity.EXTRA_ID, id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        getSeries(actorId)
    }

    private fun getSeries(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        binding.pbBuscador.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
                    .getActorSeries(id, apiKey)

                val series = response.cast.sortedByDescending { it.puntuacion }
                    .map {
                        SerieItemResponse(
                            it.id,
                            it.titulo,
                            "https://image.tmdb.org/t/p/w500${it.url}"
                        )
                    }

                withContext(Dispatchers.Main) {
                    adapter.updateList(series)
                    binding.pbBuscador.isVisible = false
                }
            } catch (e: Exception) {
                Log.e("SeriesFragment", "Error: ${e.message}")
            }
        }
    }

    private fun getSeriesCrew(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        binding.pbBuscador.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
                    .getActorSeries(id, apiKey)

                val series = response.crew.sortedByDescending { it.puntuacion }
                    .map {
                        SerieItemResponse(
                            it.id,
                            it.titulo,
                            "https://image.tmdb.org/t/p/w500${it.url}"
                        )
                    }

                withContext(Dispatchers.Main) {
                    adapter.updateList(series)
                    binding.pbBuscador.isVisible = false
                }
            } catch (e: Exception) {
                Log.e("SeriesFragment", "Error: ${e.message}")
            }
        }
    }
}
