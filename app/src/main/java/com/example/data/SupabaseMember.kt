package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseMember(
    val id: String,
    val fullName: String,
    val gender: String?,
    val beltLevel: Int?,
    val birthDate: String?,
    val joinedDate: String?,
    val phoneNumber: String?,
    val height: Double?,
    val weight: Double?,
    val status: String?,
    val notes: String?,
    val avatar: String?
)
