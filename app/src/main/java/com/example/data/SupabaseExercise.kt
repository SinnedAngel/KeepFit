package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StepDetail(
    val hint: String? = null,
    val text: String? = null,
    val type: String? = null,
    val unit: String? = null,
    val duration: Int? = null,
    val ttsCommand: String? = null,
    val waitForTTS: Boolean? = null,
    val loops: Int? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseExercise(
    val id: String,
    @Json(name = "titleEN") val titleEN: String,
    @Json(name = "titleID") val titleID: String,
    val category: String? = null,
    val difficulty: Int? = null,
    val duration: Int = 0,
    val calories: Int = 0,
    @Json(name = "descriptionEN") val descriptionEN: String? = null,
    @Json(name = "descriptionID") val descriptionID: String? = null,
    @Json(name = "stepsEN") val stepsEN: List<String>? = null,
    @Json(name = "stepsID") val stepsID: List<String>? = null,
    @Json(name = "stepDetailsEN") val stepDetailsEN: List<StepDetail>? = null,
    @Json(name = "stepDetailsID") val stepDetailsID: List<StepDetail>? = null,
    @Json(name = "videoUrl") val videoUrl: String? = null,
    @Json(name = "slidesUrl") val slidesUrl: List<String>? = null,
    val loops: Int? = null,
    @Json(name = "targetMuscles") val targetMuscles: List<String>? = null,
    @Json(name = "katedaSpecific") val katedaSpecific: Boolean? = null,
    @Json(name = "targetUnit") val targetUnit: String? = null,
    @Json(name = "targetValue") val targetValue: Int? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null
)