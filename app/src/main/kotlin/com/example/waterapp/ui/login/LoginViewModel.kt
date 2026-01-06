package com.example.waterapp.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterapp.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var state by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            state = LoginState.Error("Usuario y contraseña son requeridos")
            return
        }

        viewModelScope.launch {
            state = LoginState.Loading
            val result = repository.signIn(username, password)
            result.onSuccess { output ->
                com.example.waterapp.network.TokenManager.saveToken(output.token)
                state = LoginState.Success
                onLoginSuccess()
            }.onFailure { exception ->
                val errorMessage = when (exception) {
                    is retrofit2.HttpException -> {
                        when (exception.code()) {
                            401 -> "Usuario o contraseña incorrectos"
                            404 -> "Servicio de autenticación no encontrado"
                            500 -> "Error interno del servidor. Inténtalo más tarde"
                            else -> "Error del servidor: ${exception.code()}"
                        }
                    }
                    is java.net.ConnectException, is java.net.UnknownHostException -> 
                        "No se pudo conectar al servidor. Verifica tu conexión"
                    is java.net.SocketTimeoutException -> 
                        "La conexión ha expirado. Inténtalo de nuevo"
                    else -> exception.message ?: "Ocurrió un error desconocido"
                }
                state = LoginState.Error(errorMessage)
            }
        }
    }
}
