package com.example.movielife

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PeliculaAdapter(
    var peliculasList: List<PeliculaItemResponse> = emptyList(),
    private val onItemSelected: (Int) -> Unit
) :
    RecyclerView.Adapter<PeliculaViewHolder>() {

    fun updateList(peliculasList: List<PeliculaItemResponse>) {
        this.peliculasList = peliculasList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        return PeliculaViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        )
    }

    override fun getItemCount() = peliculasList.size


    override fun onBindViewHolder(viewHolder: PeliculaViewHolder, position: Int) {
        viewHolder.bind(peliculasList[position], onItemSelected)
    }


}