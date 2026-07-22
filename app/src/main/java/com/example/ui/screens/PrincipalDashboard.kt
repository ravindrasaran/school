package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalDashboard(
    viewModel: TimetableViewModel,
    onNavigateToGrid: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAi: () -> Unit,
    onLogout: () -> Unit
) {
    val settingsRaw by viewModel.settingsFlow.collectAsStateWithLifecycle()
    val settings = settingsRaw ?: SchoolSettings()
    val classes by viewModel.allClasses.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val cells by viewModel.allCells.collectAsStateWithLifecycle()
    val leaves by viewModel.allLeaves.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Analytics calculations
    val totalTeachers = teachers.size
    val totalClasses = classes.size
    val activeLeaves = leaves.filter { it.status == "Approved" }.size
    val pendingLeaves = leaves.filter { it.status == "Pending" }

    // Count conflicts
    val conflictsCount = remember(cells) {
        val booked = mutableMapOf<String, String>() // Key: "periodName-dayOfWeek-teacherName", Value: "className"
        var clashing = 0
        val counted = mutableSetOf<String>()
        cells.forEach { cell ->
            if (cell.teacherName != "None" && cell.teacherName.isNotEmpty()) {
                val key = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}"
                if (booked.containsKey(key)) {
                    val conflictKey1 = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}-${cell.className}"
                    val conflictKey2 = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}-${booked[key]}"
                    if (!counted.contains(conflictKey1)) {
                        clashing++
                        counted.add(conflictKey1)
                    }
                    if (!counted.contains(conflictKey2)) {
                        clashing++
                        counted.add(conflictKey2)
                    }
                } else {
                    booked[key] = cell.className
                }
            }
        }
        clashing
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(settings.schoolName, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "School Settings")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Hero Title Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = settings.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Principal & Administrator Control Deck",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Stats Row
            Text(
                "SCHOOL STATISTICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Classes",
                    value = totalClasses.toString(),
                    icon = Icons.Default.Class,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Teachers",
                    value = totalTeachers.toString(),
                    icon = Icons.Default.Person,
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Clashes",
                    value = conflictsCount.toString(),
                    icon = Icons.Default.Dangerous,
                    color = if (conflictsCount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "On Leave",
                    value = activeLeaves.toString(),
                    icon = Icons.Default.EventBusy,
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Navigation Grid
            Text(
                "CORE MODULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                QuickNavButton(
                    title = "Interactive Master Grid",
                    subtitle = "View, search, and edit schedules class-wise",
                    icon = Icons.Default.GridView,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToGrid,
                    tag = "nav_master_grid"
                )
                QuickNavButton(
                    title = "Data Management Center",
                    subtitle = "Manage Classes, Teachers, Periods & Subjects",
                    icon = Icons.Default.FolderOpen,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToData,
                    tag = "nav_data_mgmt"
                )
                QuickNavButton(
                    title = "AI Scheduler Assistant",
                    subtitle = "Resolve conflicts & ask Gemini for optimal replacements",
                    icon = Icons.Default.AutoAwesome,
                    color = Color(0xFFEC4899),
                    onClick = onNavigateToAi,
                    tag = "nav_ai_assistant"
                )
            }

            // Action: Auto-schedule
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "One-Click Auto Schedule",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Generates a complete conflict-free school schedule automatically using scheduling constraints.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            viewModel.runAutoScheduler()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.testTag("one_click_schedule_btn")
                    ) {
                        Text("Generate")
                    }
                }
            }

            // Pending Leave Requests
            if (pendingLeaves.isNotEmpty()) {
                Text(
                    "PENDING TEACHER LEAVES (${pendingLeaves.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                pendingLeaves.forEach { leave ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(leave.teacherName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Requested: ${leave.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { viewModel.approveLeave(leave) }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = Color(0xFF16A34A))
                                }
                                IconButton(onClick = { viewModel.rejectLeave(leave) }) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SchoolSettingsDialog(
            settings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { updatedSettings ->
                viewModel.updateSettings(
                    schoolName = updatedSettings.schoolName,
                    title = updatedSettings.title,
                    prayerName = updatedSettings.prayerName,
                    prayerTime = updatedSettings.prayerTime,
                    intervalName = updatedSettings.intervalName,
                    intervalTime = updatedSettings.intervalTime,
                    intervalAfterPeriod = updatedSettings.intervalAfterPeriod
                )
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun QuickNavButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSettingsDialog(
    settings: SchoolSettings,
    onDismiss: () -> Unit,
    onSave: (SchoolSettings) -> Unit
) {
    var schoolName by remember { mutableStateOf(settings.schoolName) }
    var title by remember { mutableStateOf(settings.title) }
    var prayerName by remember { mutableStateOf(settings.prayerName) }
    var prayerTime by remember { mutableStateOf(settings.prayerTime) }
    var intervalName by remember { mutableStateOf(settings.intervalName) }
    var intervalTime by remember { mutableStateOf(settings.intervalTime) }
    var intervalAfterPeriod by remember { mutableStateOf(settings.intervalAfterPeriod.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val valAfter = intervalAfterPeriod.toIntOrNull() ?: 4
                onSave(
                    SchoolSettings(
                        schoolName = schoolName,
                        title = title,
                        prayerName = prayerName,
                        prayerTime = prayerTime,
                        intervalName = intervalName,
                        intervalTime = intervalTime,
                        intervalAfterPeriod = valAfter
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("School Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School Name") },
                    modifier = Modifier.fillMaxWidth().testTag("setting_school_name")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Timetable Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prayerName,
                    onValueChange = { prayerName = it },
                    label = { Text("Prayer/Assembly Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prayerTime,
                    onValueChange = { prayerTime = it },
                    label = { Text("Prayer Time (HH:MM - HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = intervalName,
                    onValueChange = { intervalName = it },
                    label = { Text("Interval Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = intervalTime,
                    onValueChange = { intervalTime = it },
                    label = { Text("Interval Time") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = intervalAfterPeriod,
                    onValueChange = { intervalAfterPeriod = it },
                    label = { Text("Interval After Period #") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
