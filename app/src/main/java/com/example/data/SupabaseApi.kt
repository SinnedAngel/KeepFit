package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface SupabaseApi {
    @GET("exercises")
    suspend fun getExercises(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*"
    ): List<SupabaseExercise>
}
