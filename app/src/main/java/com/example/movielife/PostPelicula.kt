package com.example.movielife

data class PostPelicula(
    val peliculaId: Int = 0,
    val uid: String = "",
    val comentario: String = "",
    val valoracion: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val posterPath: String = ""
)