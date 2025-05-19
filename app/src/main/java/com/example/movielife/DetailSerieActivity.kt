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
import com.example.movielife.databinding.ActivityDetailSerieBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private lateinit var posterPath : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSerieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra(EXTRA_ID, 0)

        getSerieData(id)
        getPosts(id)
        adapter = ActorAdapter{navigateToActorDetail(it)}
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter

        binding.recyclerViewPost.setHasFixedSize(true)
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        binding.fab.setOnClickListener {
            val bottomSheet = SerieActionsBottomSheet(serieId = id, posterPath = posterPath) { watchlist, watched, comment, rating ->

                Log.d("SerieActions", "Watchlist: $watchlist, Watched: $watched, Comment: $comment, Rating: $rating")
            }
            bottomSheet.show(supportFragmentManager, "SerieActionsBottomSheet")
        }

        binding.backButton.setOnClickListener{
            finish()
        }
    }

    private fun getPosts(id: Int) {
        val database = FirebaseDatabase.getInstance()
        val seriesPostRef = database.getReference("series").child(id.toString()).child("postsseries")

        seriesPostRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postIds = mutableListOf<String>()
                for (child in snapshot.children) {
                    val key = child.key
                    if (key != null) {
                        postIds.add(key)
                        Log.d("PostLog", "Post ID encontrado: $key")
                    }
                }

                if (postIds.isEmpty()) {
                    Log.d("PostLog", "No se encontraron posts para la serie con ID $id")
                    binding.recyclerViewPost.adapter = PostSerieAdapter(emptyList(), emptyMap())
                    return
                }

                val postsRef = database.getReference("postsseries")
                val postList = mutableListOf<PostPelicula>()
                val uidSet = mutableSetOf<String>()
                var fetchedPosts = 0

                for (postId in postIds) {
                    postsRef.child(postId).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(postSnapshot: DataSnapshot) {
                            val post = postSnapshot.getValue(PostPelicula::class.java)
                            if (post != null) {
                                postList.add(post)
                                uidSet.add(post.uid)
                                Log.d("PostLog", "Post recuperado: ${post.comentario} (UID: ${post.uid})")
                            } else {
                                Log.d("PostLog", "Post nulo para ID: $postId")
                            }

                            fetchedPosts++
                            if (fetchedPosts == postIds.size) {
                                Log.d("PostLog", "Total de posts recuperados: ${postList.size}")
                                fetchUsersAndSetAdapter(postList, uidSet)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("PostLog", "Error recuperando post: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostLog", "Error accediendo a referencias de posts: ${error.message}")
            }
        })
    }

    private fun fetchUsersAndSetAdapter(postList: List<PostPelicula>, uidSet: Set<String>) {
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("usuarios")
        val userMap = mutableMapOf<String, User>()
        var fetchedUsers = 0

        if (uidSet.isEmpty()) {
            Log.d("PostLog", "No hay usuarios a recuperar")
            binding.recyclerViewPost.adapter = PostSerieAdapter(postList, userMap)
            return
        }

        for (uid in uidSet) {
            Log.d("PostLog", "Recuperando datos del usuario: $uid")
            usuariosRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        userMap[uid] = user
                        Log.d("PostLog", "Usuario recuperado: ${user.nombreUsuario}")
                    } else {
                        Log.d("PostLog", "Usuario nulo para UID: $uid")
                    }

                    fetchedUsers++
                    if (fetchedUsers == uidSet.size) {
                        Log.d("PostLog", "Usuarios recuperados: ${userMap.size}")
                        binding.recyclerViewPost.adapter = PostSerieAdapter(postList, userMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PostLog", "Error recuperando usuario: ${error.message}")
                }
            })
        }
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

        posterPath = "https://image.tmdb.org/t/p/original/" + body.url
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