package com.example.movielife.ui.adapters

import android.content.Intent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.ui.profile.OtherUserProfileActivity
import com.example.movielife.R
import com.example.movielife.model.User

class SearchUserAdapter(
    private var listaUsuarios: List<User>
) : RecyclerView.Adapter<SearchUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPerfil: ImageView = itemView.findViewById(R.id.imgPerfil)
        val textNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val textPeliculas: TextView = itemView.findViewById(R.id.tvPeliculas)
        val textSeries: TextView = itemView.findViewById(R.id.tvSeries)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cuenta, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.textNombre.text = usuario.nombreUsuario
        holder.textPeliculas.text = "Películas: ${usuario.peliculasVistas.size}"
        holder.textSeries.text = "Series: ${usuario.seriesVistas.size}"

        // Imagen del perfil
        val contexto = holder.itemView.context
        val imgPerfilId = contexto.resources.getIdentifier(usuario.fotoPerfil, "drawable", contexto.packageName)
        holder.imgPerfil.setImageResource(if (imgPerfilId != 0) imgPerfilId else R.drawable.ic_launcher_foreground)

        holder.itemView.setOnClickListener{
            val intent = Intent(contexto, OtherUserProfileActivity::class.java)
            intent.putExtra("uid", usuario.uid)
            contexto.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<User>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}
