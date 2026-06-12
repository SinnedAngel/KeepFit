package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SupabaseClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: SupabaseApi by lazy {
        var baseStr = BuildConfig.SUPABASE_URL
        if (baseStr.endsWith("/exercises")) {
            baseStr = baseStr.removeSuffix("exercises")
        }
        if (!baseStr.contains("rest/v1")) {
            baseStr = baseStr.removeSuffix("/") + "/rest/v1/"
        } else if (!baseStr.endsWith("/")) {
            baseStr += "/"
        }
        
        Retrofit.Builder()
            .baseUrl(baseStr)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }
}
