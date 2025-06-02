package com.example.movielife.ui.actor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.example.movielife.R
import com.example.movielife.ui.movies.DetailPeliculaActivity.Companion.EXTRA_ID
import com.example.movielife.databinding.ActivityDetailActorBinding
import com.example.movielife.model.ActorDetailsResponse
import com.example.movielife.model.ApiService
import com.example.movielife.model.PeliculaItemResponse
import com.example.movielife.model.SerieItemResponse
import com.example.movielife.ui.adapters.PeliculaAdapter
import com.example.movielife.ui.adapters.PeliculaSerieViewPagerAdapter
import com.example.movielife.ui.movies.DetailPeliculaActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActorActivity : AppCompatActivity() {

    companion object {
        const val ACTOR_ID = "actor_id"
        const val EXTRA_ROLE = "role"
    }

    private lateinit var binding: ActivityDetailActorBinding

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailActorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra(ACTOR_ID, 0)
        val role = intent.getStringExtra(EXTRA_ROLE)

        getActorData(id)

        // Hacer expansible la bio
        binding.tvBiografia.setOnClickListener {
            if (isExpanded) {
                binding.tvBiografia.maxLines = 3  // Contraer
            } else {
                binding.tvBiografia.maxLines = Int.MAX_VALUE  // Expandir
            }
            isExpanded = !isExpanded
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.viewPager.adapter = role?.let { PeliculaSerieViewPagerAdapter(this, id, it) }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Películas" else "Series"
        }.attach()


    }

    // Obtengo la informacion del actor a traves del id que recibo del intent
    private fun getActorData(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"

        CoroutineScope(Dispatchers.IO).launch {
            val actorDetail =
                getRetrofit().create(ApiService::class.java).getActorDetails(id, apiKey)

            runOnUiThread {
                createUI(
                    actorDetail
                )

            }
        }
    }

    private fun createUI(body: ActorDetailsResponse) {
        //Imagen poster de fondo
        Picasso.get().load("https://image.tmdb.org/t/p/original/" + body.profilePath)
            .into(binding.ivActorPrincipal);

        Picasso.get().load("https://image.tmdb.org/t/p/original/" + body.profilePath)
            .into(binding.ivActorFondo);

        // Resto de datos
        binding.tvNombre.text = body.name
        binding.tvFecha.text = body.placeOfBirth
        binding.tvBiografia.text = body.biography
        if (binding.tvBiografia.text.isEmpty()) {
            binding.tvBiografia.text = getString(R.string.no_existe_informacion)
            binding.tvBiografia.setTypeface(null, android.graphics.Typeface.NORMAL)
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


