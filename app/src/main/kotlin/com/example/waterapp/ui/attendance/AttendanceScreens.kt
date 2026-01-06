package com.example.waterapp.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import com.example.waterapp.network.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onBack: () -> Unit,
    onJobClick: (UUID, String, String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var jobs by remember { mutableStateOf<List<JobOutputDto>>(emptyList()) }
    var scheduledDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val dates = RetrofitClient.jobsApi.getScheduledDates()
            scheduledDates = dates.mapNotNull { 
                try { LocalDate.parse(it) } catch (e: Exception) { null } 
            }.toSet()
        } catch (e: Exception) {
            error = "Error al cargar fechas: ${e.message}"
        }
    }

    LaunchedEffect(selectedDate) {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.jobsApi.getJobs(
                    date = selectedDate.toString()
                )
                jobs = response.content
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Asistencia de Trabajos") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
            }
        )

        // Custom Highlightable Calendar
        HighlightableCalendar(
            selectedDate = selectedDate,
            scheduledDates = scheduledDates,
            onDateSelected = { selectedDate = it }
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Debug: Mostrar cuántas fechas se cargaron
        Text(
            text = "Fechas con tareas: ${scheduledDates.size}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        HorizontalDivider()

        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text(
                text = "Trabajos para el $selectedDate:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (jobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay trabajos programados.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs) { job ->
                        JobItem(job = job, onClick = { onJobClick(job.id, job.name, selectedDate.toString()) })
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightableCalendar(
    selectedDate: LocalDate,
    scheduledDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        // Month Selector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val daysInMonth = currentMonth.lengthOfMonth()
        
        val days = mutableListOf<LocalDate?>()
        repeat(firstDayOfWeek - 1) { days.add(null) }
        for (i in 1..daysInMonth) {
            days.add(currentMonth.atDay(i))
        }
        
        val rows = days.chunked(7)
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { date ->
                    val isScheduled = date != null && scheduledDates.contains(date)
                    val isSelected = date != null && date == selectedDate
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    date == null -> Color.Transparent
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isScheduled -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = date != null) {
                                date?.let { onDateSelected(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isScheduled -> MaterialTheme.colorScheme.onTertiaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isScheduled) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isScheduled) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                else MaterialTheme.colorScheme.tertiary
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
                // Fill remaining cells in the last row
                if (row.size < 7) {
                    repeat(7 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}



@Composable
fun JobItem(job: JobOutputDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = job.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            job.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text = "Fecha: ${job.startDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingListScreen(
    onBack: () -> Unit,
    onMeetingClick: (UUID, String, String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var meetings by remember { mutableStateOf<List<MeetingOutputDto>>(emptyList()) }
    var scheduledDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val dates = RetrofitClient.meetingsApi.getScheduledDates()
            scheduledDates = dates.mapNotNull { 
                try { LocalDate.parse(it) } catch (e: Exception) { null } 
            }.toSet()
        } catch (e: Exception) {
            error = "Error al cargar reuniones: ${e.message}"
        }
    }

    LaunchedEffect(selectedDate) {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.meetingsApi.getMeetings(
                    date = selectedDate.toString()
                )
                meetings = response.content
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Asistencia de Reuniones") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
            }
        )

        // Custom Highlightable Calendar
        HighlightableCalendar(
            selectedDate = selectedDate,
            scheduledDates = scheduledDates,
            onDateSelected = { selectedDate = it }
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Debug info
        Text(
            text = "Fechas con reuniones: ${scheduledDates.size}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        HorizontalDivider()

        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text(
                text = "Reuniones para el $selectedDate:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reuniones programadas.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(meetings) { meeting ->
                        MeetingItem(
                            meeting = meeting,
                            onClick = { onMeetingClick(meeting.id, meeting.name, selectedDate.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingItem(meeting: MeetingOutputDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = meeting.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            meeting.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text = "Fecha: ${meeting.meetingDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAttendanceScreen(
    id: UUID,
    name: String,
    date: String,
    type: String, // "job" or "meeting"
    onBack: () -> Unit
) {
    var attendances by remember { mutableStateOf<List<AttendanceState>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val savingPartners = remember { mutableStateMapOf<UUID, Boolean>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id, date) {
        isLoading = true
        scope.launch {
            try {
                if (type == "job") {
                    val list = RetrofitClient.jobsApi.getJobAttendance(id, date)
                    attendances = list.map { 
                        AttendanceState(
                            it.partnerId, 
                            it.partnerName,
                            it.partnerNumber,
                            it.present,
                            it.checkInTime?.substringAfter("T")?.take(5),
                            it.checkOutTime?.substringAfter("T")?.take(5)
                        ) 
                    }.sortedBy { it.partnerNumber ?: Long.MAX_VALUE }
                } else {
                    val list = RetrofitClient.meetingsApi.getMeetingAttendance(id, date)
                    attendances = list.map { 
                        AttendanceState(
                            it.partnerId, 
                            it.partnerName,
                            it.partnerNumber,
                            it.present,
                            it.checkInTime?.substringAfter("T")?.take(5),
                            it.checkOutTime?.substringAfter("T")?.take(5)
                        ) 
                    }.sortedBy { it.partnerNumber ?: Long.MAX_VALUE }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    val savePartner = { item: AttendanceState ->
        savingPartners[item.partnerId] = true
        scope.launch {
            try {
                if (type == "job") {
                    RetrofitClient.jobsApi.recordBulkAttendance(
                        id,
                        BulkJobAttendanceInputDto(
                            jobId = id,
                            attendanceDate = date,
                            attendances = listOf(
                                JobAttendanceItemInputDto(
                                    item.partnerId, 
                                    item.present,
                                    item.checkInTime?.let { t -> "${date}T$t:00Z" },
                                    item.checkOutTime?.let { t -> "${date}T$t:00Z" }
                                )
                            )
                        )
                    )
                } else {
                    RetrofitClient.meetingsApi.recordBulkAttendance(
                        id,
                        BulkMeetingAttendanceInputDto(
                            meetingId = id,
                            attendanceDate = date,
                            attendances = listOf(
                                MeetingAttendanceItemInputDto(
                                    item.partnerId, 
                                    item.present,
                                    item.checkInTime?.let { t -> "${date}T$t:00Z" },
                                    item.checkOutTime?.let { t -> "${date}T$t:00Z" }
                                )
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                savingPartners[item.partnerId] = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Tomar Asistencia") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = if (type == "job") "Trabajo" else "Reunión", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por número o nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredAttendances = attendances.filter { item ->
                    if (searchQuery.isEmpty()) {
                        true
                    } else {
                        // Check if query is numeric for exact number match
                        val numericQuery = searchQuery.toLongOrNull()
                        if (numericQuery != null) {
                            item.partnerNumber == numericQuery
                        } else {
                            // Text search in partner name
                            item.partnerName.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }.sortedBy { it.partnerNumber ?: Long.MAX_VALUE }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAttendances) { item ->
                        AttendanceRow(
                            item = item,
                            isSaving = savingPartners[item.partnerId] ?: false,
                            onToggle = { checked ->
                                val updated = item.copy(present = checked)
                                attendances = attendances.map { if (it.partnerId == item.partnerId) updated else it }
                                savePartner(updated)
                            },
                            onCheckInChange = { time ->
                                val updated = item.copy(checkInTime = time)
                                attendances = attendances.map { if (it.partnerId == item.partnerId) updated else it }
                                savePartner(updated)
                            },
                            onCheckOutChange = { time ->
                                val updated = item.copy(checkOutTime = time)
                                attendances = attendances.map { if (it.partnerId == item.partnerId) updated else it }
                                savePartner(updated)
                            }
                        )
                    }
                }
            }
        }
    }
}

data class AttendanceState(
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val present: Boolean,
    val checkInTime: String? = null,
    val checkOutTime: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceRow(
    item: AttendanceState,
    isSaving: Boolean,
    onToggle: (Boolean) -> Unit,
    onCheckInChange: (String?) -> Unit,
    onCheckOutChange: (String?) -> Unit
) {
    val currentTimeStr = {
        val now = java.time.LocalTime.now()
        "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                if (item.present) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (item.partnerNumber != null) "#${item.partnerNumber} - ${item.partnerName}" else item.partnerName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.present) FontWeight.Bold else FontWeight.Normal
            )
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp).padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Switch(checked = item.present, onCheckedChange = onToggle, enabled = !isSaving)
        }

        if (item.present) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entrada
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { if (item.checkInTime == null && !isSaving) onCheckInChange(currentTimeStr()) },
                        enabled = !isSaving,
                        shape = MaterialTheme.shapes.small,
                        color = if (item.checkInTime != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Entrada", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = item.checkInTime ?: "Marcar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (item.checkInTime != null) {
                        IconButton(
                            onClick = { if (!isSaving) onCheckInChange(null) },
                            enabled = !isSaving,
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Salida
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { if (item.checkOutTime == null && !isSaving) onCheckOutChange(currentTimeStr()) },
                        enabled = !isSaving,
                        shape = MaterialTheme.shapes.small,
                        color = if (item.checkOutTime != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Salida", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = item.checkOutTime ?: "Marcar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (item.checkOutTime != null) {
                        IconButton(
                            onClick = { if (!isSaving) onCheckOutChange(null) },
                            enabled = !isSaving,
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

