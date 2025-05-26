package com.example.movielife.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.model.ActorItemResponse
import com.example.movielife.R

class ActorAdapter(
    var actoresList: List<ActorItemResponse> = emptyList(),
    private val onItemSelected: (Int) -> Unit
) :
    RecyclerView.Adapter<ActorViewHolder>() {

    fun updateList(actoresList: List<ActorItemResponse>) {
        this.actoresList = actoresList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        return ActorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_actor, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ActorViewHolder, position: Int) {
        viewHolder.bind(actoresList[position], onItemSelected)
    }

    override fun getItemCount() = actoresList.size


}