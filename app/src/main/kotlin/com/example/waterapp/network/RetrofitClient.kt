package com.example.waterapp.network

import com.example.waterapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(AuthInterceptor())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val partnerApi: PartnerApi by lazy {
        retrofit.create(PartnerApi::class.java)
    }

    val readingApi: ReadingApi by lazy {
        retrofit.create(ReadingApi::class.java)
    }

    val jobsApi: JobsApi by lazy {
        retrofit.create(JobsApi::class.java)
    }

    val meetingsApi: MeetingsApi by lazy {
        retrofit.create(MeetingsApi::class.java)
    }
}

