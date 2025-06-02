package com.example.movielife.model

import com.google.gson.annotations.SerializedName

data class SerieDataResponse(
    @SerializedName("page") val response: String,
    @SerializedName("results") val peliculas: List<SerieItemResponse>
)

data class SerieItemResponse(
    @SerializedName("id") val serieId: Int,
    @SerializedName("name") val serieTitulo: String,
    @SerializedName("poster_path") val url: String
)

data class SeriesResponse(
    @SerializedName("cast") val cast: List<SeriesItem>,
    @SerializedName("crew") val crew: List<SeriesItem>
)

data class SeriesItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val titulo: String,
    @SerializedName("poster_path") val url: String?,
    @SerializedName("vote_average") val puntuacion: Double
)


data class SerieDetailResponse(
    @SerializedName("adult") val response: Boolean,
    @SerializedName("name") val titulo: String,
    @SerializedName("poster_path") val url: String,
    @SerializedName("vote_average") val puntuacion: Double,
    @SerializedName("overview") val sinopsis: String,
    @SerializedName("backdrop_path") val posterFondo: String,
    @SerializedName("first_air_date") val fecha: String,
    @SerializedName("number_of_seasons") val tiempo: Int,
    @SerializedName("genres") val genero: List<Genre>
)