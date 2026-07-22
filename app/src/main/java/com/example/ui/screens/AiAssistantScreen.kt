package com.example.ui.screens

import androidx.compose.animation.*
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
fun AiAssistantScreen(
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val cells by viewModel.allCells.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()

    var substituteTeacherName by remember { mutableStateOf("") }
    var showSubTeacherSelector by remember { mutableStateOf(false) }

    // Client-side detection of clashing periods
    val conflicts = remember(cells) {
        val booked = mutableMapOf<String, String>() // Key: "period-day-teacher", Value: "className"
        val list = mutableListOf<String>()
        cells.forEach { cell ->
            if (cell.teacherName != "None" && cell.teacherName.isNotEmpty()) {
                val key = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}"
                if (booked.containsKey(key)) {
                    val dayName = when (cell.dayOfWeek) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        else -> "Sat"
                    }
                    list.add("Teacher '${cell.teacherName}' is booked for Class '${cell.className}' and Class '${booked[key]}' during Period '${cell.periodName}' on $dayName.")
                } else {
                    booked[key] = cell.className
                }
            }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Timetable Copilot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Hero Intro
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text("Gemini Scheduler Copilot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Analyze conflicts, find substitute teachers, and run advanced optimizations.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            // Real-time conflicts status
            Text(
                "DETECTED SCHEDULE CONFLICTS (${conflicts.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            if (conflicts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // very light green
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Clean", tint = Color(0xFF16A34A))
                        Text("Perfect Schedule! No teacher clashes or double bookings detected.", color = Color(0xFF15803D), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                conflicts.forEach { conflict ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // very light red
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Clash", tint = Color(0xFFDC2626))
                            Text(conflict, color = Color(0xFF991B1B), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Action: AI Actions
            Text(
                "QUICK AI ANALYTICAL ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Button 1: Resolve Clashes
                Button(
                    onClick = { viewModel.askGemini("resolve_conflicts") },
                    modifier = Modifier.fillMaxWidth().testTag("ai_resolve_clashes"),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isAiLoading
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Resolve")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resolve Active Scheduling Conflicts")
                }

                // Button 2: Workload Balancing
                Button(
                    onClick = { viewModel.askGemini("optimize_workload") },
                    modifier = Modifier.fillMaxWidth().testTag("ai_balance_workload"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isAiLoading
                ) {
                    Icon(Icons.Default.Balance, contentDescription = "Balance")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze & Balance Teacher Workload Metrics")
                }

                // Section 3: Teacher substitution
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Find Best Substitute/Replacement Teacher", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showSubTeacherSelector = true },
                                modifier = Modifier.fillMaxWidth().testTag("ai_sub_teacher_btn")
                            ) {
                                Text(substituteTeacherName.ifEmpty { "Select Teacher..." })
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                            DropdownMenu(
                                expanded = showSubTeacherSelector,
                                onDismissRequest = { showSubTeacherSelector = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                teachers.filter { it.name != "None" }.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name) },
                                        onClick = {
                                            substituteTeacherName = t.name
                                            showSubTeacherSelector = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (substituteTeacherName.isNotEmpty()) {
                                    viewModel.askGemini("substitute_teacher", substituteTeacherName)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("ai_find_sub_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            enabled = substituteTeacherName.isNotEmpty() && !isAiLoading
                        ) {
                            Text("Query Gemini Replacement Options")
                        }
                    }
                }
            }

            // AI Responses Panel
            if (aiResponse.isNotEmpty() || isAiLoading) {
                Text(
                    "GEMINI ADVICE REPORT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isAiLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Gemini is reading schedule files and generating feedback...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Text(
                                text = aiResponse,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
