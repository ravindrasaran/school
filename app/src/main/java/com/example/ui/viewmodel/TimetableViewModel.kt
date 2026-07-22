package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAiAssistant
import com.example.data.local.SchoolDatabase
import com.example.data.model.*
import com.example.data.repository.TimetableRepository
import com.example.util.SmartScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimetableRepository
    
    // Core database flows
    val settings: StateFlow<SchoolSettings> = flow {
        // Fallback default setting if nothing in DB yet
        emit(SchoolSettings())
    }.combine(
        flowOf(null) // we will hook it up below
    ) { default, dbValue -> dbValue ?: default }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolSettings())

    init {
        val database = SchoolDatabase.getDatabase(application, viewModelScope)
        repository = TimetableRepository(database.schoolDao())
    }

    // Role state
    private val _selectedRole = MutableStateFlow<String?>(null) // Null means Login / Role Selection screen
    val selectedRole: StateFlow<String?> = _selectedRole.asStateFlow()

    // Teacher/Student dashboard focus selection
    private val _selectedTeacherName = MutableStateFlow<String>("")
    val selectedTeacherName: StateFlow<String> = _selectedTeacherName.asStateFlow()

    private val _selectedClassName = MutableStateFlow<String>("")
    val selectedClassName: StateFlow<String> = _selectedClassName.asStateFlow()

    // Database state flows
    val settingsFlow = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SchoolSettings()
    )

    val allClasses = repository.allClassesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTeachers = repository.allTeachersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPeriods = repository.allPeriodsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSubjects = repository.allSubjectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCells = repository.allCellsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLeaves = repository.allLeavesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHomework = repository.allHomeworkFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI suggestion state
    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Login/Role actions
    fun selectRole(role: String?) {
        _selectedRole.value = role
        // Pre-select first class/teacher if available when switching roles
        viewModelScope.launch {
            if (role == "Teacher") {
                val teachers = repository.getAllTeachers()
                val activeTeacher = teachers.firstOrNull { it.name != "None" }
                if (activeTeacher != null) {
                    _selectedTeacherName.value = activeTeacher.name
                }
            } else if (role == "Student") {
                val classes = repository.getAllClasses()
                val firstClass = classes.firstOrNull()
                if (firstClass != null) {
                    _selectedClassName.value = firstClass.name
                }
            }
        }
    }

    fun selectTeacher(name: String) {
        _selectedTeacherName.value = name
    }

    fun selectClass(name: String) {
        _selectedClassName.value = name
    }

    // Settings actions
    fun updateSettings(schoolName: String, title: String, prayerName: String, prayerTime: String, intervalName: String, intervalTime: String, intervalAfterPeriod: Int) {
        viewModelScope.launch {
            val updated = SchoolSettings(
                schoolName = schoolName,
                title = title,
                prayerName = prayerName,
                prayerTime = prayerTime,
                intervalName = intervalName,
                intervalTime = intervalTime,
                intervalAfterPeriod = intervalAfterPeriod
            )
            repository.saveSettings(updated)
        }
    }

    // Data Management
    fun addClass(className: String, level: String, stream: String) {
        viewModelScope.launch {
            repository.saveClass(SchoolClass(name = className, level = level, stream = stream))
        }
    }

    fun deleteClass(name: String) {
        viewModelScope.launch {
            repository.deleteClass(name)
        }
    }

    fun addTeacher(name: String, email: String, workloadLimit: Int) {
        viewModelScope.launch {
            repository.saveTeacher(Teacher(name = name, email = email, workloadLimit = workloadLimit))
        }
    }

    fun deleteTeacher(name: String) {
        viewModelScope.launch {
            repository.deleteTeacher(name)
        }
    }

    fun addPeriod(name: String, time: String) {
        viewModelScope.launch {
            val currentPeriods = repository.getAllPeriods()
            repository.savePeriod(Period(name = name, time = time, orderIndex = currentPeriods.size))
        }
    }

    fun deletePeriod(name: String) {
        viewModelScope.launch {
            repository.deletePeriod(name)
        }
    }

    fun addSubject(name: String) {
        viewModelScope.launch {
            repository.saveSubject(Subject(name = name))
        }
    }

    fun deleteSubject(name: String) {
        viewModelScope.launch {
            repository.deleteSubject(name)
        }
    }

    // Scheduling operations
    fun saveTimetableCell(periodName: String, className: String, dayOfWeek: Int, subjectName: String, teacherName: String) {
        viewModelScope.launch {
            if (subjectName.isEmpty() || subjectName == "Free") {
                repository.deleteCell(periodName, className, dayOfWeek)
            } else {
                repository.saveCell(
                    TimetableCell(
                        periodName = periodName,
                        className = className,
                        dayOfWeek = dayOfWeek,
                        subjectName = subjectName,
                        teacherName = teacherName
                    )
                )
            }
        }
    }

    fun resetAllCells() {
        viewModelScope.launch {
            repository.clearAllCells()
        }
    }

    fun runAutoScheduler() {
        viewModelScope.launch {
            val classes = repository.getAllClasses()
            val periods = repository.getAllPeriods()
            val teachers = repository.getAllTeachers()
            val subjects = repository.getAllSubjects()
            val leaves = repository.getAllLeaves()

            val generated = SmartScheduler.generateTimetable(
                classes = classes,
                periods = periods,
                teachers = teachers,
                subjects = subjects,
                leaves = leaves
            )

            if (generated.isNotEmpty()) {
                repository.clearAllCells()
                repository.saveCells(generated)
            }
        }
    }

    // AI assistant queries
    fun askGemini(questionType: String, additionalInfo: String = "") {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Consulting Gemini AI scheduler..."
            try {
                val teachers = repository.getAllTeachers()
                val subjects = repository.getAllSubjects()
                val classes = repository.getAllClasses()
                val periods = repository.getAllPeriods()
                val currentCells = repository.getAllCells()

                val advice = GeminiAiAssistant.getSchedulingAdvice(
                    teachers = teachers,
                    subjects = subjects,
                    classes = classes,
                    periods = periods,
                    currentCells = currentCells,
                    questionType = questionType,
                    additionalInfo = additionalInfo
                )
                _aiResponse.value = advice
            } catch (e: Exception) {
                _aiResponse.value = "Failed to run AI suggestion: ${e.localizedMessage}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Leaves
    fun submitLeave(teacherName: String, date: String) {
        viewModelScope.launch {
            repository.saveLeave(TeacherLeave(teacherName = teacherName, date = date, status = "Pending"))
        }
    }

    fun approveLeave(leave: TeacherLeave) {
        viewModelScope.launch {
            repository.saveLeave(leave.copy(status = "Approved"))
        }
    }

    fun rejectLeave(leave: TeacherLeave) {
        viewModelScope.launch {
            repository.saveLeave(leave.copy(status = "Rejected"))
        }
    }

    fun deleteLeave(id: Int) {
        viewModelScope.launch {
            repository.deleteLeave(id)
        }
    }

    // Homework
    fun addHomework(className: String, subjectName: String, title: String, description: String, dueDate: String) {
        viewModelScope.launch {
            repository.saveHomework(
                Homework(
                    className = className,
                    subjectName = subjectName,
                    title = title,
                    description = description,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteHomework(id: Int) {
        viewModelScope.launch {
            repository.deleteHomework(id)
        }
    }
}
