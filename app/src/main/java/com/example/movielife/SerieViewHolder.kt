package com.example.movielife

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.databinding.ItemPeliculaBinding
import com.squareup.picasso.Picasso

class SerieViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemPeliculaBinding.bind(view)

    fun bind(serieItemResponse: SerieItemResponse, onItemSelected: (Int) -> Unit){

        Picasso.get().load("https://image.tmdb.org/t/p/original/" + serieItemResponse.url).into(binding.ivPelicula);

        binding.root.setOnClickListener{onItemSelected(serieItemResponse.serieId) }
    }
}