package com.example.waterapp

import android.os.Bundle
import java.util.UUID
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterapp.ui.login.LoginScreen
import com.example.waterapp.ui.theme.WaterAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaterAppTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var selectedItemId by remember { mutableStateOf<UUID?>(null) }
                var selectedItemName by remember { mutableStateOf("") }
                var selectedDate by remember { mutableStateOf("") }
                var attendanceType by remember { mutableStateOf("") } // "job" or "meeting"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "login" -> LoginScreen(onLoginSuccess = { currentScreen = "home" })
                            "home" -> {
                                // En el home, el botón de atrás no hace nada especial o sale de la app
                                HomeScreen(
                                    onNavigateToConsumption = { currentScreen = "consumption" },
                                    onNavigateToJobs = { currentScreen = "jobs" },
                                    onNavigateToMeetings = { currentScreen = "meetings" },
                                    onLogout = { 
                                        com.example.waterapp.network.TokenManager.clearToken()
                                        currentScreen = "login" 
                                    }
                                )
                            }
                            "consumption" -> {
                                androidx.activity.compose.BackHandler { currentScreen = "home" }
                                com.example.waterapp.ui.consumption.ConsumptionScreen(
                                    onBack = { currentScreen = "home" }
                                )
                            }
                            "jobs" -> {
                                androidx.activity.compose.BackHandler { currentScreen = "home" }
                                com.example.waterapp.ui.attendance.JobListScreen(
                                    onBack = { currentScreen = "home" },
                                    onJobClick = { id, name, date ->
                                        selectedItemId = id
                                        selectedItemName = name
                                        selectedDate = date
                                        attendanceType = "job"
                                        currentScreen = "record_attendance"
                                    }
                                )
                            }
                            "meetings" -> {
                                androidx.activity.compose.BackHandler { currentScreen = "home" }
                                com.example.waterapp.ui.attendance.MeetingListScreen(
                                    onBack = { currentScreen = "home" },
                                    onMeetingClick = { id, name, date ->
                                        selectedItemId = id
                                        selectedItemName = name
                                        selectedDate = date
                                        attendanceType = "meeting"
                                        currentScreen = "record_attendance"
                                    }
                                )
                            }
                            "record_attendance" -> {
                                androidx.activity.compose.BackHandler { 
                                    currentScreen = if (attendanceType == "job") "jobs" else "meetings" 
                                }
                                selectedItemId?.let { id ->
                                    com.example.waterapp.ui.attendance.RecordAttendanceScreen(
                                        id = id,
                                        name = selectedItemName,
                                        date = selectedDate,
                                        type = attendanceType,
                                        onBack = { currentScreen = if (attendanceType == "job") "jobs" else "meetings" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToConsumption: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToMeetings: () -> Unit,
    onLogout: () -> Unit
) {
    val role = com.example.waterapp.network.TokenManager.getRole()?.uppercase() ?: ""
    
    // Role matching using the exact names from your JWT example
    val isAdmin = role == "ADMINISTRADOR" || role == "ADMIN"
    val isMeterReader = role == "LECTOR DE MEDIDORES" || role == "WATER_METER_READER"
    val isSecretary = role == "SECRETARIO DE ASISTENCIA" || role == "SECRETARIO_ASISTENCIA"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón Lector de Medidores
        if (isAdmin || isMeterReader) {
            Button(
                onClick = onNavigateToConsumption,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("REGISTRAR CONSUMO", fontWeight = FontWeight.Bold)
            }
            
            if (isAdmin || isSecretary) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Botones de Asistencia
        if (isAdmin || isSecretary) {
            Button(
                onClick = onNavigateToJobs,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = com.example.waterapp.ui.theme.WaterWork)
            ) {
                Text("ASISTENCIA TRABAJOS", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToMeetings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("ASISTENCIA REUNIONES", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("CERRAR SESIÓN")
        }
    }
}
