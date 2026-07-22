package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterGridScreen(
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsFlow.collectAsStateWithLifecycle()
    val classes by viewModel.allClasses.collectAsStateWithLifecycle()
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val cells by viewModel.allCells.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPeriodName by remember { mutableStateOf("") }
    var selectedClassName by remember { mutableStateOf("") }

    // Group cells for easy lookup: Key = "periodName-className-day"
    val cellMap = remember(cells) {
        cells.associateBy { "${it.periodName}-${it.className}-${it.dayOfWeek}" }
    }

    // Identify conflicts (teacher booked twice in same period & day)
    val conflicts = remember(cells) {
        val booked = mutableMapOf<String, String>() // Key: "periodName-dayOfWeek-teacherName", Value: "className"
        val clashing = mutableSetOf<String>() // Set of "periodName-className-day"
        cells.forEach { cell ->
            if (cell.teacherName != "None" && cell.teacherName.isNotEmpty()) {
                val key = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}"
                if (booked.containsKey(key)) {
                    clashing.add("${cell.periodName}-${cell.className}-${cell.dayOfWeek}")
                    clashing.add("${cell.periodName}-${booked[key]}-${cell.dayOfWeek}")
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
                title = { Text("Master Schedule Grid") },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Summary header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Interactive Matrix View",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap any cell below to view or edit the day-wise subjects and teacher allocation.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (classes.isEmpty() || periods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = "Empty", modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No classes or periods defined. Please add them in Settings.")
                    }
                }
            } else {
                // Horizontal + Vertical Scrollable Grid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Row (Classes)
                        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                            // Empty corner cell
                            CellBox(text = "Period", isHeader = true)
                            classes.forEach { cls ->
                                CellBox(text = cls.name, isHeader = true)
                            }
                        }

                        // Periods Rows
                        periods.forEach { period ->
                            Row {
                                // Period Label Cell
                                CellBox(
                                    text = "${period.name}\n${period.time}",
                                    isHeader = true
                                )

                                classes.forEach { schoolClass ->
                                    // Aggregate schedule data for this period + class across days
                                    val daysScheduled = mutableMapOf<Int, TimetableCell>()
                                    var cellHasConflict = false
                                    for (day in 1..6) {
                                        val cellKey = "${period.name}-${schoolClass.name}-$day"
                                        val c = cellMap[cellKey]
                                        if (c != null) {
                                            daysScheduled[day] = c
                                            if (conflicts.contains(cellKey)) {
                                                cellHasConflict = true
                                            }
                                        }
                                    }

                                    InteractiveCell(
                                        daysScheduled = daysScheduled,
                                        hasConflict = cellHasConflict,
                                        onClick = {
                                            selectedPeriodName = period.name
                                            selectedClassName = schoolClass.name
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        CellEditDialog(
            periodName = selectedPeriodName,
            className = selectedClassName,
            subjects = subjects,
            teachers = teachers,
            cells = cells.filter { it.periodName == selectedPeriodName && it.className == selectedClassName },
            onDismiss = { showEditDialog = false },
            onSave = { updatedSchedules ->
                // Save each day's updated schedule
                for (day in 1..6) {
                    val sched = updatedSchedules[day]
                    if (sched != null) {
                        viewModel.saveTimetableCell(
                            periodName = selectedPeriodName,
                            className = selectedClassName,
                            dayOfWeek = day,
                            subjectName = sched.subjectName,
                            teacherName = sched.teacherName
                        )
                    } else {
                        viewModel.saveTimetableCell(
                            periodName = selectedPeriodName,
                            className = selectedClassName,
                            dayOfWeek = day,
                            subjectName = "",
                            teacherName = ""
                        )
                    }
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CellBox(text: String, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 75.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .background(if (isHeader) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InteractiveCell(
    daysScheduled: Map<Int, TimetableCell>,
    hasConflict: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        hasConflict -> Color(0xFFFEE2E2) // light red for conflict
        daysScheduled.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 75.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (daysScheduled.isEmpty()) {
            Text(
                text = "—",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontSize = 14.sp
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Group by subject and teacher to show concisely
                val uniqueEntries = daysScheduled.values.groupBy { "${it.subjectName}\n${it.teacherName}" }
                uniqueEntries.entries.take(2).forEach { (label, items) ->
                    val subject = label.split("\n")[0]
                    val teacher = label.split("\n").getOrElse(1) { "" }
                    val daysStr = if (items.size < 6) " (D:${items.joinToString(",") { it.dayOfWeek.toString() }})" else ""

                    Text(
                        text = "$subject$daysStr",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        color = if (hasConflict) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                    )
                    if (teacher != "None" && teacher.isNotEmpty()) {
                        Text(
                            text = teacher,
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = if (hasConflict) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                if (uniqueEntries.size > 2) {
                    Text(
                        text = "+${uniqueEntries.size - 2} more",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellEditDialog(
    periodName: String,
    className: String,
    subjects: List<Subject>,
    teachers: List<Teacher>,
    cells: List<TimetableCell>,
    onDismiss: () -> Unit,
    onSave: (Map<Int, TimetableCell?>) -> Unit
) {
    val weekdays = listOf(
        1 to "1. Monday",
        2 to "2. Tuesday",
        3 to "3. Wednesday",
        4 to "4. Thursday",
        5 to "5. Friday",
        6 to "6. Saturday"
    )

    // Local state for each day: DayOfWeek -> (SubjectName, TeacherName)
    val localScheduleState = remember {
        mutableStateMapOf<Int, Pair<String, String>>().apply {
            weekdays.forEach { (day, _) ->
                val existing = cells.find { it.dayOfWeek == day }
                put(day, Pair(existing?.subjectName ?: "Free", existing?.teacherName ?: "None"))
            }
        }
    }

    var applyAllSubject by remember { mutableStateOf("Free") }
    var applyAllTeacher by remember { mutableStateOf("None") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val output = mutableMapOf<Int, TimetableCell?>()
                weekdays.forEach { (day, _) ->
                    val (subj, teach) = localScheduleState[day] ?: Pair("Free", "None")
                    if (subj != "Free" && subj.isNotEmpty()) {
                        output[day] = TimetableCell(
                            periodName = periodName,
                            className = className,
                            dayOfWeek = day,
                            subjectName = subj,
                            teacherName = teach
                        )
                    } else {
                        output[day] = null
                    }
                }
                onSave(output)
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text(
                text = "Edit Class $className - Period $periodName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Apply to All Row
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Apply to All Days",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subject Dropdown
                            var showSubjDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showSubjDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(applyAllSubject, maxLines = 1)
                                }
                                DropdownMenu(
                                    expanded = showSubjDropdown,
                                    onDismissRequest = { showSubjDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Free") },
                                        onClick = { applyAllSubject = "Free"; showSubjDropdown = false }
                                    )
                                    subjects.filter { it.name != "Free" }.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s.name) },
                                            onClick = { applyAllSubject = s.name; showSubjDropdown = false }
                                        )
                                    }
                                }
                            }

                            // Teacher Dropdown
                            var showTeachDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showTeachDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(applyAllTeacher, maxLines = 1)
                                }
                                DropdownMenu(
                                    expanded = showTeachDropdown,
                                    onDismissRequest = { showTeachDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = { applyAllTeacher = "None"; showTeachDropdown = false }
                                    )
                                    teachers.filter { it.name != "None" }.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name) },
                                            onClick = { applyAllTeacher = t.name; showTeachDropdown = false }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    weekdays.forEach { (day, _) ->
                                        localScheduleState[day] = Pair(applyAllSubject, applyAllTeacher)
                                    }
                                }
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }

                // Weekday inputs
                weekdays.forEach { (day, name) ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentPair = localScheduleState[day] ?: Pair("Free", "None")

                            // Day-wise Subject select
                            var showSubDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showSubDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(currentPair.first, maxLines = 1)
                                }
                                DropdownMenu(
                                    expanded = showSubDropdown,
                                    onDismissRequest = { showSubDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Free") },
                                        onClick = {
                                            localScheduleState[day] = Pair("Free", "None")
                                            showSubDropdown = false
                                        }
                                    )
                                    subjects.filter { it.name != "Free" }.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s.name) },
                                            onClick = {
                                                localScheduleState[day] = Pair(s.name, currentPair.second)
                                                showSubDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Day-wise Teacher select
                            var showTeaDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showTeaDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = currentPair.first != "Free"
                                ) {
                                    Text(if (currentPair.first == "Free") "None" else currentPair.second, maxLines = 1)
                                }
                                DropdownMenu(
                                    expanded = showTeaDropdown,
                                    onDismissRequest = { showTeaDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = {
                                            localScheduleState[day] = Pair(currentPair.first, "None")
                                            showTeaDropdown = false
                                        }
                                    )
                                    teachers.filter { it.name != "None" }.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name) },
                                            onClick = {
                                                localScheduleState[day] = Pair(currentPair.first, t.name)
                                                showTeaDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    )
}
