package com.example.waterapp.network

import android.util.Base64
import org.json.JSONObject

object TokenManager {
    private var token: String? = null
    private var role: String? = null

    fun saveToken(newToken: String) {
        token = newToken
        role = decodeRoleFromToken(newToken)
    }

    private fun decodeRoleFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            
            val payloadBase64 = parts[1]
            val decodedBytes = Base64.decode(payloadBase64, Base64.URL_SAFE)
            val payloadJson = String(decodedBytes, Charsets.UTF_8)
            
            val jsonObject = JSONObject(payloadJson)
            jsonObject.optString("role", null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getToken(): String? {
        return token
    }

    fun getRole(): String? {
        return role
    }

    fun clearToken() {
        token = null
        role = null
    }
}
