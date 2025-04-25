package com.example.movielife

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class CrewAdapter(
    var crewList: List<CrewItemResponse> = emptyList(),
    private val onItemSelected: (Int) -> Unit
) :
    RecyclerView.Adapter<CrewViewHolder>() {

    fun updateList(crewList: List<CrewItemResponse>) {
        this.crewList = crewList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        return CrewViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_actor, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: CrewViewHolder, position: Int) {
        viewHolder.bind(crewList[position], onItemSelected)
    }

    override fun getItemCount() = crewList.size


}