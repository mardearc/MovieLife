package com.example.movielife

import com.google.gson.annotations.SerializedName

data class ActorDataResponse(
        @SerializedName("id") val id :Int,
        @SerializedName("cast") val cast :List<ActorItemResponse>
        )

data class CrewDataResponse(
    @SerializedName("id") val id :Int,
    @SerializedName("crew") val cast :List<CrewItemResponse>
)

data class CrewItemResponse(
    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String,
    @SerializedName("profile_path") val url : String,
    @SerializedName("job") val character : String
)
data class ActorItemResponse(
    @SerializedName("id") val id :Int,
    @SerializedName("name") val name : String,
    @SerializedName("profile_path") val url : String,
    @SerializedName("character") val character : String
)

data class ActorDetailsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("biography") val biography: String?,
    @SerializedName("place_of_birth") val placeOfBirth: String?,
    @SerializedName("profile_path") val profilePath: String?
)