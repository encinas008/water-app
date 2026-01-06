package com.example.waterapp.network

import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class WaterMeterReadingInputDto(
    val partnerId: UUID,
    val readingDate: String, // ISO date string
    val currentReading: BigDecimal,
    val observation: String = ""
)

data class WaterMeterReadingOutputDto(
    val id: UUID,
    val partnerId: UUID,
    val partnerName: String,
    val readingDate: String,
    val currentReading: BigDecimal,
    val consumption: BigDecimal
)

interface ReadingApi {
    @POST("water-readings")
    suspend fun recordReading(@Body input: WaterMeterReadingInputDto): WaterMeterReadingOutputDto
}
