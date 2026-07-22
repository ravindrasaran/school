package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
fun DataManagementScreen(
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val classes by viewModel.allClasses.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Classes", "Teachers", "Periods", "Subjects")

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Database Manager") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_data")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ClassList(classes, onDelete = { viewModel.deleteClass(it) })
                    1 -> TeacherList(teachers, onDelete = { viewModel.deleteTeacher(it) })
                    2 -> PeriodList(periods, onDelete = { viewModel.deletePeriod(it) })
                    3 -> SubjectList(subjects, onDelete = { viewModel.deleteSubject(it) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddDataDialog(
            tabIndex = selectedTab,
            onDismiss = { showAddDialog = false },
            onAddClass = { name, level, stream ->
                viewModel.addClass(name, level, stream)
                showAddDialog = false
            },
            onAddTeacher = { name, email, limit ->
                viewModel.addTeacher(name, email, limit)
                showAddDialog = false
            },
            onAddPeriod = { name, time ->
                viewModel.addPeriod(name, time)
                showAddDialog = false
            },
            onAddSubject = { name ->
                viewModel.addSubject(name)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ClassList(classes: List<SchoolClass>, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(classes) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(c.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${c.level} | ${c.stream}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { onDelete(c.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherList(teachers: List<Teacher>, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(teachers.filter { it.name != "None" }) { t ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(t.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Limit: ${t.workloadLimit} periods/wk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { onDelete(t.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodList(periods: List<Period>, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(periods) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Period ${p.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(p.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { onDelete(p.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectList(subjects: List<Subject>, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(subjects.filter { it.name != "Free" }) { s ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { onDelete(s.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AddDataDialog(
    tabIndex: Int,
    onDismiss: () -> Unit,
    onAddClass: (String, String, String) -> Unit,
    onAddTeacher: (String, String, Int) -> Unit,
    onAddPeriod: (String, String) -> Unit,
    onAddSubject: (String) -> Unit
) {
    // Shared states
    var textVal1 by remember { mutableStateOf("") }
    var textVal2 by remember { mutableStateOf("") }
    var textVal3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                when (tabIndex) {
                    0 -> onAddClass(textVal1, textVal2.ifEmpty { "Class 9-10" }, textVal3.ifEmpty { "General" })
                    1 -> onAddTeacher(textVal1, textVal2, textVal3.toIntOrNull() ?: 24)
                    2 -> onAddPeriod(textVal1, textVal2)
                    3 -> onAddSubject(textVal1)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text(
                text = when (tabIndex) {
                    0 -> "Add Class"
                    1 -> "Add Teacher"
                    2 -> "Add Period"
                    else -> "Add Subject"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (tabIndex) {
                    0 -> { // Class fields
                        OutlinedTextField(
                            value = textVal1,
                            onValueChange = { textVal1 = it },
                            label = { Text("Class Name (e.g. X A)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_class_name")
                        )
                        OutlinedTextField(
                            value = textVal2,
                            onValueChange = { textVal2 = it },
                            label = { Text("Level (e.g. Class 9-10)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = textVal3,
                            onValueChange = { textVal3 = it },
                            label = { Text("Stream (e.g. General)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    1 -> { // Teacher fields
                        OutlinedTextField(
                            value = textVal1,
                            onValueChange = { textVal1 = it },
                            label = { Text("Teacher Name") },
                            modifier = Modifier.fillMaxWidth().testTag("add_teacher_name")
                        )
                        OutlinedTextField(
                            value = textVal2,
                            onValueChange = { textVal2 = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = textVal3,
                            onValueChange = { textVal3 = it },
                            label = { Text("Workload Limit (Periods/Wk)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> { // Period fields
                        OutlinedTextField(
                            value = textVal1,
                            onValueChange = { textVal1 = it },
                            label = { Text("Period Name (e.g. IX)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_period_name")
                        )
                        OutlinedTextField(
                            value = textVal2,
                            onValueChange = { textVal2 = it },
                            label = { Text("Time Range (e.g. 01:00 - 01:35)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> { // Subject fields
                        OutlinedTextField(
                            value = textVal1,
                            onValueChange = { textVal1 = it },
                            label = { Text("Subject Name (e.g. HISTORY)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_subject_name")
                        )
                    }
                }
            }
        }
    )
}
