package com.example.movielife

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.ActorItemResponse
import com.example.movielife.databinding.ItemActorBinding
import com.squareup.picasso.Picasso

class ActorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemActorBinding.bind(view)

    fun bind(actorItemResponse: ActorItemResponse, onItemSelected: (Int) -> Unit) {
        binding.actorName.text = actorItemResponse.name

        Picasso.get().load("https://image.tmdb.org/t/p/w500/" + actorItemResponse.url)
            .into(binding.ivActor);

        binding.actorCharacter.text = buildString {
            append("(")
            append(actorItemResponse.character)
            append(")")
        }

        itemView.setOnClickListener {
            onItemSelected(actorItemResponse.id)
        }
    }
}

