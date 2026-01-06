package com.example.waterapp.repository

import com.example.waterapp.network.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class WaterRepository(
    private val partnerApi: PartnerApi = RetrofitClient.partnerApi,
    private val readingApi: ReadingApi = RetrofitClient.readingApi
) {
    suspend fun searchPartners(query: String): Result<List<PartnerOutputDto>> {
        return try {
            val partners = partnerApi.searchPartners(query)
            Result.success(partners)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception(errorBody ?: "Error en la búsqueda (Status: ${e.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordReading(
        partnerId: UUID,
        reading: BigDecimal,
        date: LocalDate = LocalDate.now(),
        observation: String = ""
    ): Result<WaterMeterReadingOutputDto> {
        return try {
            val input = WaterMeterReadingInputDto(
                partnerId = partnerId,
                readingDate = date.toString(),
                currentReading = reading,
                observation = observation
            )
            val result = readingApi.recordReading(input)
            Result.success(result)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception(errorBody ?: "Error al registrar la lectura (Status: ${e.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
