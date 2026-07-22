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
fun TeacherDashboard(
    viewModel: TimetableViewModel,
    onLogout: () -> Unit
) {
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val selectedTeacherName by viewModel.selectedTeacherName.collectAsStateWithLifecycle()
    val cells by viewModel.allCells.collectAsStateWithLifecycle()
    val leaves by viewModel.allLeaves.collectAsStateWithLifecycle()
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showHomeworkDialog by remember { mutableStateOf(false) }

    // Filter cells where selected teacher is scheduled
    val scheduledPeriods = remember(cells, selectedTeacherName) {
        cells.filter { it.teacherName == selectedTeacherName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Portal") },
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
            // Teacher selector card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Your Profile Name", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var showTeacherSelector by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showTeacherSelector = true },
                            modifier = Modifier.fillMaxWidth().testTag("teacher_select_btn")
                        ) {
                            Text(selectedTeacherName.ifEmpty { "Select Teacher..." })
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                        }
                        DropdownMenu(
                            expanded = showTeacherSelector,
                            onDismissRequest = { showTeacherSelector = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            teachers.filter { it.name != "None" }.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.name) },
                                    onClick = {
                                        viewModel.selectTeacher(t.name)
                                        showTeacherSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CardTravel, contentDescription = "Leave")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Leave", fontSize = 12.sp)
                }
                Button(
                    onClick = { showHomeworkDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Homework")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Homework", fontSize = 12.sp)
                }
            }

            // Personalized timetable list
            Text(
                "YOUR PERSONALIZED WEEKLY TIMETABLE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            if (scheduledPeriods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No classes scheduled for you currently.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    val daySchedules = scheduledPeriods.filter { it.dayOfWeek == dayNum }
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
                                
                                daySchedules.forEach { s ->
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
                                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                            Text("Class ${s.className}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Leaves request history list
            val teacherLeaves = leaves.filter { it.teacherName == selectedTeacherName }
            if (teacherLeaves.isNotEmpty()) {
                Text(
                    "LEAVE REQUEST STATUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                teacherLeaves.forEach { leave ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Requested for Date: ${leave.date}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Status: ${leave.status}", fontSize = 11.sp)
                            }
                            val badgeColor = when (leave.status) {
                                "Approved" -> Color(0xFF22C55E)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(leave.status, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLeaveDialog) {
        var leaveDate by remember { mutableStateOf("2026-07-20") }
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.submitLeave(selectedTeacherName, leaveDate)
                    showLeaveDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            },
            title = { Text("Request Teacher Leave") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select leave date for '$selectedTeacherName':", fontSize = 13.sp)
                    OutlinedTextField(
                        value = leaveDate,
                        onValueChange = { leaveDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().testTag("leave_date_input")
                    )
                }
            }
        )
    }

    if (showHomeworkDialog) {
        var hwClass by remember { mutableStateOf("") }
        var hwSubj by remember { mutableStateOf("") }
        var hwTitle by remember { mutableStateOf("") }
        var hwDesc by remember { mutableStateOf("") }
        var hwDue by remember { mutableStateOf("2026-07-25") }

        AlertDialog(
            onDismissRequest = { showHomeworkDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (hwClass.isNotEmpty() && hwSubj.isNotEmpty()) {
                        viewModel.addHomework(hwClass, hwSubj, hwTitle, hwDesc, hwDue)
                    }
                    showHomeworkDialog = false
                }) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHomeworkDialog = false }) { Text("Cancel") }
            },
            title = { Text("Upload Class Homework") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = hwClass,
                        onValueChange = { hwClass = it },
                        label = { Text("Class (e.g. X A)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hwClass, // simple mapping logic
                        onValueChange = { hwSubj = it },
                        label = { Text("Subject (e.g. MATHS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hwTitle,
                        onValueChange = { hwTitle = it },
                        label = { Text("Homework Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hwDesc,
                        onValueChange = { hwDesc = it },
                        label = { Text("Homework Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hwDue,
                        onValueChange = { hwDue = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
