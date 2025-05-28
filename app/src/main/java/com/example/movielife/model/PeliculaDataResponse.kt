package com.example.movielife.model

import com.google.gson.annotations.SerializedName

data class PeliculaDataResponse(
    @SerializedName("page") val response: String,
    @SerializedName("results") val peliculas: List<PeliculaItemResponse>
)

data class PeliculaItemResponse(
    @SerializedName("id") val peliculaId: Int,
    @SerializedName("title") val peliculaTitulo: String?,
    @SerializedName("poster_path") val url: String
)

data class PeliculaDetailResponse(
    @SerializedName("adult") val response: Boolean,
    @SerializedName("title") val titulo: String,
    @SerializedName("poster_path") val url: String,
    @SerializedName("vote_average") val puntuacion: Double,
    @SerializedName("overview") val sinopsis: String,
    @SerializedName("backdrop_path") val posterFondo: String,
    @SerializedName("release_date") val fecha: String,
    @SerializedName("runtime") val tiempo: Int,
    @SerializedName("genres") val genero: List<Genre>
)

data class Genre(
    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String,

    )

data class PeliculaPlataformas(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: Map<String, Country>
)

data class Country(
    @SerializedName("flatrate") val flatrate: List<Provider>?,
    @SerializedName("buy") val buy: List<Provider>?,
    @SerializedName("rent") val rent: List<Provider>?
)

data class Provider(
    @SerializedName("provider_id") val provider_id:Int,
    @SerializedName("logo_path") val logo_path: String,
)


data class MovieCreditsResponse(
    @SerializedName("cast") val cast: List<PeliculaResponse>
)

data class MovieCrewResponse(
    @SerializedName("crew") val crew: List<PeliculaResponse>
)

data class PeliculaResponse(
    @SerializedName("adult") val response: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String,
    @SerializedName("poster_path") val url: String,
    @SerializedName("vote_average") val puntuacion: Double,
    @SerializedName("overview") val sinopsis: String,
    @SerializedName("backdrop_path") val posterFondo: String,
    @SerializedName("release_date") val fecha: String,
    @SerializedName("runtime") val tiempo: Int,
    @SerializedName("genres") val genero: List<Genre>
)

