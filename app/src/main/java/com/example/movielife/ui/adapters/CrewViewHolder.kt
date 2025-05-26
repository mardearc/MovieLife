package com.example.movielife.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.model.CrewItemResponse
import com.example.movielife.databinding.ItemActorBinding
import com.squareup.picasso.Picasso

class CrewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemActorBinding.bind(view)

    fun bind(crewItemResponse: CrewItemResponse, onItemSelected: (Int) -> Unit) {
        binding.actorName.text = crewItemResponse.name

        Picasso.get().load("https://image.tmdb.org/t/p/w500/" + crewItemResponse.url)
            .into(binding.ivActor);

        binding.actorCharacter.text = buildString {
            append("(")
            append(crewItemResponse.character)
            append(")")
        }

        itemView.setOnClickListener {
            onItemSelected(crewItemResponse.id)
        }
    }
}