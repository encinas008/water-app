package com.example.waterapp.network

import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

data class PartnerOutputDto(
    val id: UUID,
    val partnerNumber: Long?,
    val fullName: String,
    val waterMeterNumber: String?,
    val address: String?
)

interface PartnerApi {
    @GET("partners/search")
    suspend fun searchPartners(@Query("q") query: String): List<PartnerOutputDto>
}
