package com.example.movielife

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PostAdapter(
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

    }

    override fun getItemCount(): Int = postList.size
}
