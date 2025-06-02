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
import com.example.movielife.databinding.FragmentDetailActorPeliculaBinding
import com.example.movielife.model.ApiService
import com.example.movielife.model.PeliculaItemResponse
import com.example.movielife.ui.adapters.PeliculaAdapter
import com.example.movielife.ui.movies.DetailPeliculaActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActorPeliculaFragment : Fragment() {

    private var actorId: Int = 0
    private var role: String ="actor"
    private lateinit var adapter: PeliculaAdapter
    private lateinit var binding: FragmentDetailActorPeliculaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actorId = arguments?.getInt("actor_id") ?: 0
        role = arguments?.getString("role").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailActorPeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Recuperar actorId y role
    companion object {
        fun newInstance(actorId: Int, role: String): DetailActorPeliculaFragment {
            val fragment = DetailActorPeliculaFragment()
            fragment.arguments = Bundle().apply {
                putInt("actor_id", actorId)
                putString("role", role)
            }
            return fragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PeliculaAdapter { id ->
            val intent = Intent(requireContext(), DetailPeliculaActivity::class.java)
            intent.putExtra(DetailPeliculaActivity.EXTRA_ID, id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        if (role == "actor") {
            getPeliculas(actorId)
        } else if (role == "crew") {
            getPeliculasCrew(actorId)
        }
    }

    // Obtener peliculas de actores
    private fun getPeliculas(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"
        binding.pbBuscador.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = getRetrofit()
                    .create(ApiService::class.java)
                    .getActorMovies(id, apiKey)

                val peliculas = response.cast.sortedByDescending { it.puntuacion }
                    .map {
                        PeliculaItemResponse(
                            it.id,
                            it.titulo,
                            "https://image.tmdb.org/t/p/w500${it.url}"
                        )
                    }

                withContext(Dispatchers.Main) {
                    adapter.updateList(peliculas)
                    binding.pbBuscador.isVisible = false
                }
            } catch (e: Exception) {
                Log.e("PeliculasFragment", "Error: ${e.message}")
            }
        }
    }

    // Obtener peliculas de crew
    private fun getPeliculasCrew(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"

        binding.pbBuscador.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val peliculaDetail = getRetrofit()
                    .create(ApiService::class.java)
                    .getMovieCrew(id, apiKey)
                Log.e("MovieDetails", "Lanzando request")
                if (peliculaDetail.crew.isNotEmpty()) {
                    val crewList = peliculaDetail.crew
                        .sortedByDescending { it.puntuacion }
                    Log.e("MovieDetails", "Buscando.")
                    val peliculas = crewList
                        .distinctBy { it.id }.map { movie ->
                            PeliculaItemResponse(
                                peliculaId = movie.id,
                                peliculaTitulo = movie.titulo,
                                url = movie.url.let { "https://image.tmdb.org/t/p/w500$it" }
                            )
                        }

                    withContext(Dispatchers.Main) {
                        adapter.updateList(peliculas)
                        binding.pbBuscador.isVisible = false
                    }
                } else {
                    Log.e("MovieDetails", "El crew está vacío.")
                }
            } catch (e: Exception) {
                Log.e("MovieDetails", "Error en películas de crew: ${e.message}")
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
}
