package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // Settings
    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SchoolSettings?>

    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SchoolSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SchoolSettings)

    // Classes
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClassesFlow(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes ORDER BY name ASC")
    suspend fun getAllClasses(): List<SchoolClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Query("DELETE FROM classes WHERE name = :name")
    suspend fun deleteClass(name: String)

    @Query("DELETE FROM classes")
    suspend fun deleteAllClasses()

    // Teachers
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachersFlow(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers ORDER BY name ASC")
    suspend fun getAllTeachers(): List<Teacher>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Query("DELETE FROM teachers WHERE name = :name")
    suspend fun deleteTeacher(name: String)

    @Query("DELETE FROM teachers")
    suspend fun deleteAllTeachers()

    // Periods
    @Query("SELECT * FROM periods ORDER BY orderIndex ASC")
    fun getAllPeriodsFlow(): Flow<List<Period>>

    @Query("SELECT * FROM periods ORDER BY orderIndex ASC")
    suspend fun getAllPeriods(): List<Period>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: Period)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriods(periods: List<Period>)

    @Query("DELETE FROM periods WHERE name = :name")
    suspend fun deletePeriod(name: String)

    @Query("DELETE FROM periods")
    suspend fun deleteAllPeriods()

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjectsFlow(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getAllSubjects(): List<Subject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Query("DELETE FROM subjects WHERE name = :name")
    suspend fun deleteSubject(name: String)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    // Timetable Cells
    @Query("SELECT * FROM timetable_cells")
    fun getAllCellsFlow(): Flow<List<TimetableCell>>

    @Query("SELECT * FROM timetable_cells")
    suspend fun getAllCells(): List<TimetableCell>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCell(cell: TimetableCell)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCells(cells: List<TimetableCell>)

    @Query("DELETE FROM timetable_cells WHERE periodName = :periodName AND className = :className AND dayOfWeek = :dayOfWeek")
    suspend fun deleteCell(periodName: String, className: String, dayOfWeek: Int)

    @Query("DELETE FROM timetable_cells WHERE className = :className")
    suspend fun deleteCellsForClass(className: String)

    @Query("DELETE FROM timetable_cells")
    suspend fun deleteAllCells()

    // Teacher Leaves
    @Query("SELECT * FROM teacher_leaves ORDER BY date DESC")
    fun getAllLeavesFlow(): Flow<List<TeacherLeave>>

    @Query("SELECT * FROM teacher_leaves ORDER BY date DESC")
    suspend fun getAllLeaves(): List<TeacherLeave>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: TeacherLeave)

    @Query("DELETE FROM teacher_leaves WHERE id = :id")
    suspend fun deleteLeave(id: Int)

    // Homework Entries
    @Query("SELECT * FROM homework_entries ORDER BY dueDate ASC")
    fun getAllHomeworkFlow(): Flow<List<Homework>>

    @Query("SELECT * FROM homework_entries ORDER BY dueDate ASC")
    suspend fun getAllHomework(): List<Homework>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework)

    @Query("DELETE FROM homework_entries WHERE id = :id")
    suspend fun deleteHomework(id: Int)
}
