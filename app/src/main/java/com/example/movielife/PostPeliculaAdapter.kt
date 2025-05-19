package com.example.movielife

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.DetailPeliculaActivity.Companion.EXTRA_ID
import com.squareup.picasso.Picasso

class PostPeliculaAdapter(
    private val postList: List<PostPelicula>,
    private val userMap: Map<String, User>
) : RecyclerView.Adapter<PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val user = userMap[post.uid]

        holder.username.text = user?.nombreUsuario ?: "Anónimo"
        holder.postContent.text = post.comentario
        holder.ratingBar.rating = post.valoracion.toFloat()

        // Imagen del perfil
        val contexto = holder.itemView.context
        val imgPerfilId = contexto.resources.getIdentifier(user?.fotoPerfil, "drawable", contexto.packageName)
        holder.imagePerfil.setImageResource(if (imgPerfilId != 0) imgPerfilId else R.drawable.ic_launcher_foreground)

        Picasso.get().load("https://image.tmdb.org/t/p/w500${post.posterPath}").into(holder.imgMoviePoster)

        holder.imgMoviePoster.setOnClickListener{
            val intent = Intent(contexto, DetailPeliculaActivity::class.java)
            intent.putExtra(EXTRA_ID, post.peliculaId)
            contexto.startActivity(intent)
        }

        holder.imagePerfil.setOnClickListener{
            val intent = Intent(contexto, OtherUserProfileActivity::class.java)
            intent.putExtra("uid", post.uid)
            contexto.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = postList.size
}
