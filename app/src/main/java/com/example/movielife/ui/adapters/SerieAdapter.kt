package com.example.movielife.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.R
import com.example.movielife.model.SerieItemResponse

class SerieAdapter(
    var peliculasList: List<SerieItemResponse> = emptyList(),
    private val onItemSelected: (Int) -> Unit
) :
    RecyclerView.Adapter<SerieViewHolder>() {

    fun updateList(peliculasList: List<SerieItemResponse>) {
        this.peliculasList = peliculasList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerieViewHolder {
        return SerieViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        )
    }

    override fun getItemCount() = peliculasList.size


    override fun onBindViewHolder(viewHolder: SerieViewHolder, position: Int) {
        viewHolder.bind(peliculasList[position], onItemSelected)
    }


}