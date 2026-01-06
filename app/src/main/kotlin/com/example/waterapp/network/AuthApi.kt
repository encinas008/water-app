package com.example.waterapp.network

import retrofit2.http.Body
import retrofit2.http.POST
import java.util.UUID

data class AuthenticationInputDto(
    val username: String,
    val password: String
)

data class AuthenticationOutputDto(
    val token: String,
    val refreshToken: UUID
)

interface AuthApi {
    @POST("auth/sign-in")
    suspend fun signIn(@Body input: AuthenticationInputDto): AuthenticationOutputDto
}
