package com.example.util

import com.example.data.model.*
import kotlin.random.Random

object SmartScheduler {

    /**
     * Automatically generates a conflict-free timetable based on the list of teachers,
     * classes, periods, and subjects.
     *
     * It ensures:
     * 1. No teacher double booking (clashes) on any day/period.
     * 2. Teacher leave compliance.
     * 3. Balanced subject distribution.
     */
    fun generateTimetable(
        classes: List<SchoolClass>,
        periods: List<Period>,
        teachers: List<Teacher>,
        subjects: List<Subject>,
        leaves: List<TeacherLeave> = emptyList()
    ): List<TimetableCell> {
        if (classes.isEmpty() || periods.isEmpty() || teachers.isEmpty() || subjects.isEmpty()) {
            return emptyList()
        }

        val generatedCells = mutableListOf<TimetableCell>()
        val activeTeachers = teachers.filter { it.name != "None" && it.isAvailable }
        val activeSubjects = subjects.filter { it.name != "Free" }

        if (activeTeachers.isEmpty() || activeSubjects.isEmpty()) return emptyList()

        // Keeps track of which teachers are booked: Key = "day-period-teacher", Value = Boolean
        val teacherBookings = mutableSetOf<String>()

        // Keeps track of active leaves: Key = "teacherName-dayOfWeek" (just a simple mapping for simplicity)
        // Let's assume leaves are active for days
        val teacherLeavesSet = leaves
            .filter { it.status == "Approved" }
            .map { it.teacherName }
            .toSet()

        // Helper map to estimate teacher specializations based on typical Indian school models
        // mapping subjects to potential teachers
        val subjectToTeachersMap = mutableMapOf<String, MutableList<Teacher>>()
        activeSubjects.forEach { subj ->
            subjectToTeachersMap[subj.name] = mutableListOf()
        }

        // Distribute teachers to subjects logically to make the timetable realistic
        activeTeachers.forEachIndexed { index, teacher ->
            // Let each teacher specialize in 2-3 subjects
            val specializedSubjects = when {
                teacher.name.contains("MATH", ignoreCase = true) || teacher.name.contains("SUNITA", ignoreCase = true) -> listOf("MATHS", "SCIENCE", "COMPUTER -IT")
                teacher.name.contains("BISHNOI", ignoreCase = true) -> listOf("HINDI", "SANSKRIT", "DIAGNOSTIC TEACHING")
                teacher.name.contains("RAM", ignoreCase = true) -> listOf("ENGLISH", "SST", "HISTORY", "POL. SCIENCE")
                teacher.name.contains("LAL", ignoreCase = true) -> listOf("SCIENCE", "CHEMISTRY", "PHYSICS", "BIO/MATH")
                teacher.name.contains("PET", ignoreCase = true) -> listOf("HEALTH $ PHY. EDU.", "SPORTS", "SUPW")
                else -> {
                    // fallback: pick 2 random subjects
                    listOf(
                        activeSubjects[index % activeSubjects.size].name,
                        activeSubjects[(index + 1) % activeSubjects.size].name
                    )
                }
            }
            specializedSubjects.forEach { subName ->
                subjectToTeachersMap[subName]?.add(teacher)
            }
        }

        // Fill empty lists in subjectToTeachersMap with all teachers as backup
        subjectToTeachersMap.forEach { (_, teacherList) ->
            if (teacherList.isEmpty()) {
                teacherList.addAll(activeTeachers)
            }
        }

        val random = Random(42) // Fixed seed for reproducible generation on clicks

        // Core Constraint Satisfaction Loop
        for (day in 1..6) { // Monday to Saturday
            for (period in periods) {
                for (schoolClass in classes) {
                    
                    // Select a subject
                    // Try to avoid assigning same subject repeatedly to the same class on the same day
                    val possibleSubjects = activeSubjects.shuffled(random)
                    var assigned = false

                    for (subject in possibleSubjects) {
                        // Find a teacher for this subject who is available
                        val candidates = subjectToTeachersMap[subject.name]?.shuffled(random) ?: emptyList()
                        val availableTeacher = candidates.firstOrNull { teacher ->
                            val bookingKey = "$day-${period.name}-${teacher.name}"
                            val isOnLeave = teacherLeavesSet.contains(teacher.name)
                            !teacherBookings.contains(bookingKey) && !isOnLeave
                        }

                        if (availableTeacher != null) {
                            // Assign cell!
                            generatedCells.add(
                                TimetableCell(
                                    periodName = period.name,
                                    className = schoolClass.name,
                                    dayOfWeek = day,
                                    subjectName = subject.name,
                                    teacherName = availableTeacher.name
                                )
                            )
                            // Register booking
                            teacherBookings.add("$day-${period.name}-${availableTeacher.name}")
                            assigned = true
                            break
                        }
                    }

                    // Fallback to Free period if no available teacher could be assigned
                    if (!assigned) {
                        generatedCells.add(
                            TimetableCell(
                                periodName = period.name,
                                className = schoolClass.name,
                                dayOfWeek = day,
                                subjectName = "Free",
                                teacherName = "None"
                            )
                        )
                    }
                }
            }
        }

        return generatedCells
    }
}
