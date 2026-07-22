package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    viewModel: TimetableViewModel,
    onLogout: () -> Unit
) {
    val classes by viewModel.allClasses.collectAsStateWithLifecycle()
    val selectedClassName by viewModel.selectedClassName.collectAsStateWithLifecycle()
    val cells by viewModel.allCells.collectAsStateWithLifecycle()
    val homework by viewModel.allHomework.collectAsStateWithLifecycle()
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val settingsRaw by viewModel.settingsFlow.collectAsStateWithLifecycle()
    val settings = settingsRaw ?: SchoolSettings()

    // Filter cells scheduled for the selected class
    val classCells = remember(cells, selectedClassName) {
        cells.filter { it.className == selectedClassName }
    }

    // Filter homework assignments for the selected class
    val classHomework = homework.filter { it.className.equals(selectedClassName, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student & Parent Portal") },
                actions = {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Class selector card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Your Class / Section", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var showClassSelector by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showClassSelector = true },
                            modifier = Modifier.fillMaxWidth().testTag("student_class_select_btn")
                        ) {
                            Text(selectedClassName.ifEmpty { "Select Class..." })
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                        }
                        DropdownMenu(
                            expanded = showClassSelector,
                            onDismissRequest = { showClassSelector = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            classes.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        viewModel.selectClass(c.name)
                                        showClassSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Prayer and Interval Timings Quick Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Schedule", tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Daily Assembly & Recess", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${settings.prayerName}: ${settings.prayerTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("${settings.intervalName}: ${settings.intervalTime} (After Period ${settings.intervalAfterPeriod})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            // Timetable display
            Text(
                "CLASS WEEKLY TIMETABLE SCHEDULE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            if (classCells.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No schedule uploaded for Class $selectedClassName yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val weekdays = listOf(
                    1 to "Monday",
                    2 to "Tuesday",
                    3 to "Wednesday",
                    4 to "Thursday",
                    5 to "Friday",
                    6 to "Saturday"
                )

                weekdays.forEach { (dayNum, dayName) ->
                    val daySchedules = classCells.filter { it.dayOfWeek == dayNum }
                    if (daySchedules.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(dayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                daySchedules.sortedBy { s ->
                                    periods.find { it.name == s.periodName }?.orderIndex ?: 0
                                }.forEach { s ->
                                    val time = periods.find { it.name == s.periodName }?.time ?: ""
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Period ${s.periodName} ($time)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(s.subjectName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text(s.teacherName, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Homework Assignments
            Text(
                "PENDING CLASS HOMEWORK & TASKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            if (classHomework.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No pending homework assignments registered.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                classHomework.forEach { hw ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(hw.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                    Text("Due: ${hw.dueDate}", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(hw.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Subject: ${hw.subjectName}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
