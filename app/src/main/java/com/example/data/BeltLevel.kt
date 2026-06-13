package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BeltLevel(
    val id: Int,
    val nameEN: String,
    val nameID: String,
    val color: String
)
