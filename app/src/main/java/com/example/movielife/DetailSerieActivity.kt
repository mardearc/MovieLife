package com.example.movielife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielife.DetailActorActivity.Companion.ACTOR_ID
import com.example.movielife.databinding.ActivityDetailPeliculaBinding
import com.example.movielife.databinding.ActivityDetailSerieBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailSerieActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    private lateinit var binding: ActivityDetailSerieBinding

    private lateinit var adapter: ActorAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSerieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra(EXTRA_ID, 0)

        getSerieData(id)
        adapter = ActorAdapter{navigateToActorDetail(it)}
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
    }


    private fun getSerieData(id: Int) {
        val apiKey = "cef2d5efc3c68480cb48f48b33b29de4"

        //Obtener los datos a través del id
        CoroutineScope(Dispatchers.IO).launch {
            val serieDetail =
                getRetrofit().create(ApiService::class.java).getSerieById(id, apiKey)
            if (serieDetail.body() != null) {
                runOnUiThread {
                    createUI(
                        serieDetail.body()!!
                    )

                }
            } else {
                Log.i("SerieDetails", "Error: ${serieDetail.code()}")
            }


            // Obtener los logos de la plataforma de cada serie
            val peliculaPlatform =
                getRetrofit().create(ApiService::class.java).getPlataformasSeries(id, apiKey)

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
                        val cardView = CardView(this@DetailSerieActivity)
                        cardView.layoutParams = params
                        cardView.radius = 8f

                        val imageView = ImageView(this@DetailSerieActivity)

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
                getRetrofit().create(ApiService::class.java).getSerieCredits(id, apiKey)

            if (actorDetail.isSuccessful) {
                val actores = actorDetail.body()
                if (actores != null) {
                    runOnUiThread {
                        adapter.updateList(actores.cast)
                    }

                }
            }

        }
    }

    private fun sinPlataforma() {
        val noPlataformasTextView = TextView(this@DetailSerieActivity)
        noPlataformasTextView.text = getString(R.string.no_hay_plataformas)
        noPlataformasTextView.textSize = 16f
        binding.plataformaLogosLayout.addView(noPlataformasTextView)
    }


    private fun createUI(body: SerieDetailResponse) {

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
            if(body.tiempo == 1){
                append(" temporada")
            }else{
                append(" temporadas")
            }
            append(" • ")
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