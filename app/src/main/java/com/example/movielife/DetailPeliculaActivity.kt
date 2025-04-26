package com.example.movielife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.DetailActorActivity.Companion.ACTOR_ID
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import com.example.movielife.databinding.ActivityDetailPeliculaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailPeliculaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    private lateinit var binding: ActivityDetailPeliculaBinding

    private lateinit var adapterActor: ActorAdapter

    private lateinit var adapterCrew: CrewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPeliculaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra(EXTRA_ID, 0)

        getPeliculaData(id)
        adapterActor = ActorAdapter{navigateToActorDetail(it)}
        binding.recyclerViewActor.setHasFixedSize(true)
        binding.recyclerViewActor.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewActor.adapter = adapterActor

        adapterCrew = CrewAdapter{navigateToActorDetail(it)}
        binding.recyclerViewCrew.setHasFixedSize(true)
        binding.recyclerViewCrew.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCrew.adapter = adapterCrew

        binding.fab.setOnClickListener {
            val bottomSheet = MovieActionsBottomSheet(movieId = id) { watchlist, watched, comment, rating ->

                Log.d("MovieActions", "Watchlist: $watchlist, Watched: $watched, Comment: $comment, Rating: $rating")
            }
            bottomSheet.show(supportFragmentManager, "MovieActionsBottomSheet")
        }

        binding.backButton.setOnClickListener{
            finish()
        }

    }


    private fun getPeliculaData(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"

        //Obtener los datos a través del id
        CoroutineScope(Dispatchers.IO).launch {
            val peliculaDetail =
                getRetrofit().create(ApiService::class.java).getMovieById(id, apiKey)
            if (peliculaDetail.body() != null) {
                runOnUiThread {
                    createUI(
                        peliculaDetail.body()!!
                    )

                }
            } else {
                Log.i("MovieDetails", "Error: ${peliculaDetail.code()}")
            }


            // Obtener los logos de la plataforma de cada película
            val peliculaPlatform =
                getRetrofit().create(ApiService::class.java).getPlataformas(id, apiKey)

            val logos = binding.plataformaLogosLayout

            if (peliculaPlatform.isSuccessful) {
                runOnUiThread {
                    val plataformas = peliculaPlatform.body()?.results?.get("ES")

                    // Configurar la separación entre los logos en el LinearLayout
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(8, 0, 0, 0)  // Separación entre logos

                    // Si no tiene plataforma
                    if (plataformas?.flatrate == null) {
                        sinPlataforma()
                    }
                    plataformas?.flatrate?.forEach { plat ->

                        // Creo CardView con cada logo
                        val cardView = CardView(this@DetailPeliculaActivity)
                        cardView.layoutParams = params
                        cardView.radius = 8f

                        val imageView = ImageView(this@DetailPeliculaActivity)

                        val logoUrl = "https://image.tmdb.org/t/p/w200${plat.logo_path}"
                        Picasso.get().load(logoUrl).into(imageView)

                        val imageSize = 150
                        imageView.layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)

                        // Agregar el ImageView al CardView
                        cardView.addView(imageView)

                        // Agregar el CardView al LinearLayout
                        logos.addView(cardView)

                    }
                }
            } else {

                Log.i("mardearc", "No hay logo")
            }

            // Reparto
            val actorDetail =
                getRetrofit().create(ApiService::class.java).getMovieCredits(id, apiKey)

            if (actorDetail.isSuccessful) {
                val actores = actorDetail.body()
                if (actores != null) {
                    runOnUiThread {
                        adapterActor.updateList(actores.cast)
                    }

                }
            }

            // Crew

            val crewDetail =
                getRetrofit().create(ApiService::class.java).getMovieCrew(id, apiKey)

            if (crewDetail.isSuccessful) {
                val crew = crewDetail.body()
                if (crew != null) {
                    runOnUiThread {
                        adapterCrew.updateList(crew.cast)
                    }

                }
            }

        }
    }

    private fun sinPlataforma() {
        val noPlataformasTextView = TextView(this@DetailPeliculaActivity)
        noPlataformasTextView.text = getString(R.string.no_hay_plataformas)
        noPlataformasTextView.textSize = 16f
        binding.plataformaLogosLayout.addView(noPlataformasTextView)
    }


    private fun createUI(body: PeliculaDetailResponse) {

        //Imagen poster de fondo
        Picasso.get().load("https://image.tmdb.org/t/p/original/" + body.url)
            .into(binding.ivDetalleFondo);
        //Imagen poster
        Picasso.get().load("https://image.tmdb.org/t/p/original/" + body.url)
            .into(binding.ivDetalle);

        //Imagen fondo de la informacion / Imagen secundaria
        Picasso.get().load("https://image.tmdb.org/t/p/original/" + body.posterFondo)
            .into(binding.ivFondo)

        // Añadir el FrameLayout al CardView
        binding.tvDetallitos.text = buildString {
            append(body.fecha.substring(0, 4))  //Formatear año de la fecha
            append(" • ")
            append(body.tiempo)
            append("min • ")
            append(body.genero.joinToString(", ") { it.name }) //Seleccionar todos los géneros
        }
        binding.tvTitulo.text = body.titulo
        binding.tvSinopsis.text = body.sinopsis
        binding.rbPuntuacion.rating = body.puntuacion.toFloat()
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    private fun navigateToActorDetail(id: Int) {
        val intent = Intent(this, DetailActorActivity::class.java)
        intent.putExtra(ACTOR_ID, id)
        startActivity(intent)
    }
}