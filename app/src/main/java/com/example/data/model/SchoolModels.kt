package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "school_settings")
data class SchoolSettings(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "GOVT. SR. SEC. SCHOOL HADETAR, SANCHORE",
    val title: String = "MASTER TIMETABLE SESSION 2025-26",
    val prayerName: String = "MORNING PRAYER",
    val prayerTime: String = "07:30 - 07:55",
    val intervalName: String = "INTERVAL",
    val intervalTime: String = "10:15 - 10:40",
    val intervalAfterPeriod: Int = 4
) : Serializable

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey val name: String,
    val level: String = "Class 9-10", // Primary (1-5), Upper Primary (6-8), Secondary (9-10), Senior Secondary (11-12)
    val stream: String = "General" // Science, Commerce, Arts, Vocational
) : Serializable

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val name: String,
    val email: String = "",
    val workloadLimit: Int = 24, // periods per week
    val isAvailable: Boolean = true
) : Serializable

@Entity(tableName = "periods")
data class Period(
    @PrimaryKey val name: String,
    val time: String,
    val orderIndex: Int
) : Serializable

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val name: String
) : Serializable

@Entity(tableName = "timetable_cells", primaryKeys = ["periodName", "className", "dayOfWeek"])
data class TimetableCell(
    val periodName: String,
    val className: String,
    val dayOfWeek: Int, // 1 to 6 (Monday to Saturday)
    val subjectName: String,
    val teacherName: String
) : Serializable

@Entity(tableName = "teacher_leaves")
data class TeacherLeave(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherName: String,
    val date: String, // YYYY-MM-DD
    val status: String = "Pending" // Pending, Approved, Rejected
) : Serializable

@Entity(tableName = "homework_entries")
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String,
    val subjectName: String,
    val title: String,
    val description: String,
    val dueDate: String
) : Serializable
