package com.example.movielife.ui.adapters

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movielife.databinding.ItemPostBinding

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemPostBinding.bind(view)

    val imagePerfil: ImageView = binding.imageViewMenuLateral
    val username: TextView = binding.tvUsername
    val ratingBar: RatingBar = binding.ratingBar
    val imgMoviePoster: ImageView = binding.imgMoviePoster
    val postContent: TextView = binding.tvPostContent
}