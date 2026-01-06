package com.example.waterapp.repository

import com.example.waterapp.network.AuthApi
import com.example.waterapp.network.AuthenticationInputDto
import com.example.waterapp.network.AuthenticationOutputDto
import com.example.waterapp.network.RetrofitClient

class AuthRepository(private val api: AuthApi = RetrofitClient.authApi) {
    suspend fun signIn(username: String, password: String): Result<AuthenticationOutputDto> {
        return try {
            val response = api.signIn(AuthenticationInputDto(username, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
