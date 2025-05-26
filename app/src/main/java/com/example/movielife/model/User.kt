package com.example.movielife.model

data class User(
    val uid: String = "",
    val nombreUsuario: String = "",
    val fotoPerfil: String = "",
    val watchlistPeliculas: List<String> = listOf(),
    val peliculasVistas: List<String> = listOf(),
    val watchlistSeries: List<String> = listOf(),
    val seriesVistas: List<String> = listOf(),
    val postspeliculas: Map<String, Boolean>? = null
)
