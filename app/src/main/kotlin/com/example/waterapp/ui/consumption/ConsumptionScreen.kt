package com.example.waterapp.ui.consumption

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterapp.network.PartnerOutputDto
import com.example.waterapp.ui.theme.GhostWhite
import com.example.waterapp.ui.theme.WaterPrimaryLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    onBack: () -> Unit,
    viewModel: ConsumptionViewModel = viewModel()
) {
    val state = viewModel.state
    val selectedPartner = viewModel.selectedPartner
    var searchQuery by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state is ConsumptionState.Saved) {
            reading = ""
            observation = ""
            scope.launch {
                snackbarHostState.showSnackbar("¡Lectura registrada con éxito!")
            }
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registro de Consumo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (selectedPartner == null) {
                // Search Section
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchPartners(it)
                    },
                    label = { Text("Buscar Socio (Nro. Socio)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    is ConsumptionState.Searching -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ConsumptionState.Results -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(state.partners) { partner ->
                                PartnerItem(partner) {
                                    viewModel.selectPartner(partner)
                                }
                            }
                        }
                    }
                    is ConsumptionState.NoResults -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Ups! No existe el socio.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No encontramos ningún resultado para \"$searchQuery\".\nVerifica el número e intenta de nuevo.",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    is ConsumptionState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {
                        Text("Ingresa el número de socio para comenzar", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                // Detail and Entry Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WaterPrimaryLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Socio Seleccionado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedPartner.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Nro. Socio: ${selectedPartner.partnerNumber ?: "N/A"}")
                        Text("Nro. Medidor: ${selectedPartner.waterMeterNumber ?: "N/A"}")
                        if (!selectedPartner.address.isNullOrBlank()) {
                            Text("Dirección: ${selectedPartner.address}")
                        }
                        
                        TextButton(
                            onClick = { viewModel.clearSelectedPartner() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cambiar Socio")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = reading,
                    onValueChange = { input ->
                        // Permitir solo números y un punto decimal o coma
                        val cleanInput = input.replace(',', '.')
                        if (cleanInput.isEmpty() || cleanInput.matches(Regex("""^\d*[.]?\d{0,2}$"""))) {
                            // Validar máximo 9,999,999.99
                            val numValue = cleanInput.toDoubleOrNull() ?: 0.0
                            if (numValue <= 9999999.99) {
                                reading = cleanInput
                            } else {
                                reading = "9999999.99"
                            }
                        }
                    },
                    label = { Text("Nueva Lectura (m3)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(32.dp))

                val readingValue = reading.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = reading.isNotBlank() && readingValue >= 1.0 && state !is ConsumptionState.Saving
                ) {
                    if (state is ConsumptionState.Saving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("REGISTRAR CONSUMO", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (state is ConsumptionState.Error) {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }

    if (showConfirmDialog && selectedPartner != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Registro") },
            text = {
                Column {
                    Text("¿Estás seguro de registrar la lectura para ${selectedPartner.fullName}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lectura: $reading m3",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.recordReading(reading, observation)
                    }
                ) {
                    Text("CONFIRMAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

@Composable
fun PartnerItem(partner: PartnerOutputDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(partner.fullName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Nro. Socio: ${partner.partnerNumber ?: "N/A"} - Medidor: ${partner.waterMeterNumber ?: "N/A"}", 
            fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        if (partner.connectionStatusCode == "CUT_OFF") {
            Text("ESTADO: CORTADO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        }
        Divider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}
