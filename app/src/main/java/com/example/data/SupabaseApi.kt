package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface SupabaseApi {
    @GET("exercises")
    suspend fun getExercises(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): List<SupabaseExercise>

    @GET("belt_levels")
    suspend fun getBeltLevels(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*"
    ): List<BeltLevel>

    @GET("members")
    suspend fun getMemberById(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<SupabaseMember>
}