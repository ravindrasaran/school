package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class TimetableRepository(private val schoolDao: SchoolDao) {

    // Settings
    val settingsFlow: Flow<SchoolSettings?> = schoolDao.getSettingsFlow()
    suspend fun getSettings(): SchoolSettings? = schoolDao.getSettings()
    suspend fun saveSettings(settings: SchoolSettings) = schoolDao.insertSettings(settings)

    // Classes
    val allClassesFlow: Flow<List<SchoolClass>> = schoolDao.getAllClassesFlow()
    suspend fun getAllClasses(): List<SchoolClass> = schoolDao.getAllClasses()
    suspend fun saveClass(schoolClass: SchoolClass) = schoolDao.insertClass(schoolClass)
    suspend fun deleteClass(name: String) = schoolDao.deleteClass(name)

    // Teachers
    val allTeachersFlow: Flow<List<Teacher>> = schoolDao.getAllTeachersFlow()
    suspend fun getAllTeachers(): List<Teacher> = schoolDao.getAllTeachers()
    suspend fun saveTeacher(teacher: Teacher) = schoolDao.insertTeacher(teacher)
    suspend fun deleteTeacher(name: String) = schoolDao.deleteTeacher(name)

    // Periods
    val allPeriodsFlow: Flow<List<Period>> = schoolDao.getAllPeriodsFlow()
    suspend fun getAllPeriods(): List<Period> = schoolDao.getAllPeriods()
    suspend fun savePeriod(period: Period) = schoolDao.insertPeriod(period)
    suspend fun deletePeriod(name: String) = schoolDao.deletePeriod(name)

    // Subjects
    val allSubjectsFlow: Flow<List<Subject>> = schoolDao.getAllSubjectsFlow()
    suspend fun getAllSubjects(): List<Subject> = schoolDao.getAllSubjects()
    suspend fun saveSubject(subject: Subject) = schoolDao.insertSubject(subject)
    suspend fun deleteSubject(name: String) = schoolDao.deleteSubject(name)

    // Timetable Cells
    val allCellsFlow: Flow<List<TimetableCell>> = schoolDao.getAllCellsFlow()
    suspend fun getAllCells(): List<TimetableCell> = schoolDao.getAllCells()
    suspend fun saveCell(cell: TimetableCell) = schoolDao.insertCell(cell)
    suspend fun deleteCell(periodName: String, className: String, dayOfWeek: Int) =
        schoolDao.deleteCell(periodName, className, dayOfWeek)
    suspend fun saveCells(cells: List<TimetableCell>) = schoolDao.insertCells(cells)
    suspend fun clearAllCells() = schoolDao.deleteAllCells()

    // Leaves
    val allLeavesFlow: Flow<List<TeacherLeave>> = schoolDao.getAllLeavesFlow()
    suspend fun getAllLeaves(): List<TeacherLeave> = schoolDao.getAllLeaves()
    suspend fun saveLeave(leave: TeacherLeave) = schoolDao.insertLeave(leave)
    suspend fun deleteLeave(id: Int) = schoolDao.deleteLeave(id)

    // Homework
    val allHomeworkFlow: Flow<List<Homework>> = schoolDao.getAllHomeworkFlow()
    suspend fun getAllHomework(): List<Homework> = schoolDao.getAllHomework()
    suspend fun saveHomework(homework: Homework) = schoolDao.insertHomework(homework)
    suspend fun deleteHomework(id: Int) = schoolDao.deleteHomework(id)
}
