package com.example.movielife

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("search/movie")
    suspend fun getPeliculas(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("query") peliculaName: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<PeliculaDataResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieById(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PeliculaDetailResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieByIdToList(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PeliculaItemResponse>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<PeliculaDataResponse>

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getPlataformas(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<PeliculaPlataformas>

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<ActorDataResponse>

    @GET("person/{person_id}")
    suspend fun getActorDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): ActorDetailsResponse

    @GET("person/{person_id}/movie_credits")
    suspend fun getActorMovies(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): MovieCreditsResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCrew(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<CrewDataResponse>


    // Series
    @GET("search/tv")
    suspend fun getSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("query") peliculaName: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<SerieDataResponse>

    @GET("tv/popular")
    suspend fun getPopularSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<SerieDataResponse>

    @GET("tv/{id}")
    suspend fun getSerieById(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<SerieDetailResponse>

    @GET("tv/{id}/watch/providers")
    suspend fun getPlataformasSeries(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<PeliculaPlataformas>

    @GET("tv/{id}/credits")
    suspend fun getSerieCredits(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<ActorDataResponse>
}

