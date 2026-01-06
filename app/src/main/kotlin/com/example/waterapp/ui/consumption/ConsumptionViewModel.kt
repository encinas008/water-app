package com.example.waterapp.ui.consumption

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterapp.network.PartnerOutputDto
import com.example.waterapp.repository.WaterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class ConsumptionState {
    object Idle : ConsumptionState()
    object Searching : ConsumptionState()
    data class Results(val partners: List<PartnerOutputDto>) : ConsumptionState()
    object NoResults : ConsumptionState()
    data class Error(val message: String) : ConsumptionState()
    object Saving : ConsumptionState()
    object Saved : ConsumptionState()
}

class ConsumptionViewModel(private val repository: WaterRepository = WaterRepository()) : ViewModel() {
    var state by mutableStateOf<ConsumptionState>(ConsumptionState.Idle)
        private set

    var selectedPartner by mutableStateOf<PartnerOutputDto?>(null)
        private set

    private var searchJob: Job? = null

    fun searchPartners(query: String) {
        if (query.isBlank()) {
            state = ConsumptionState.Idle
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            state = ConsumptionState.Searching
            val result = repository.searchPartners(query)
            result.onSuccess { partners ->
                // Filtrar solo por coincidencia EXACTA de partnerNumber
                val filteredPartners = partners.filter { partner ->
                    partner.partnerNumber?.toString() == query
                }
                
                if (filteredPartners.isEmpty()) {
                    state = ConsumptionState.NoResults
                } else {
                    state = ConsumptionState.Results(filteredPartners)
                }
            }.onFailure {
                state = ConsumptionState.Error(it.message ?: "Error desconocido en la búsqueda")
            }
        }
    }

    fun selectPartner(partner: PartnerOutputDto) {
        selectedPartner = partner
        state = ConsumptionState.Idle
    }

    fun clearSelectedPartner() {
        selectedPartner = null
        state = ConsumptionState.Idle
    }

    fun recordReading(reading: String, observation: String) {
        val partnerId = selectedPartner?.id ?: return
        val readingValue = reading.toBigDecimalOrNull()
        
        if (readingValue == null) {
            state = ConsumptionState.Error("La lectura debe ser un número válido")
            return
        }

        viewModelScope.launch {
            state = ConsumptionState.Saving
            val result = repository.recordReading(partnerId, readingValue, observation = observation)
            result.onSuccess {
                state = ConsumptionState.Saved
                selectedPartner = null // Reset after success
            }.onFailure {
                state = ConsumptionState.Error(it.message ?: "Ocurrió un error inesperado al guardar")
            }
        }
    }
    
    fun resetState() {
        state = ConsumptionState.Idle
    }
}
