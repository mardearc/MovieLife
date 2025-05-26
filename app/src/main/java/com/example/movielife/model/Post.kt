package com.example.movielife.model

data class Post(
    val peliculaId: Int = 0,
    val uid: String = "",
    val comentario: String = "",
    val valoracion: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val posterPath: String = "",
    val tipo: String = ""
)