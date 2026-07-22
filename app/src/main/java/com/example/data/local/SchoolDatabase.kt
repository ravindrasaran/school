package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SchoolSettings::class,
        SchoolClass::class,
        Teacher::class,
        Period::class,
        Subject::class,
        TimetableCell::class,
        TeacherLeave::class,
        Homework::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "school_timetable_database"
                )
                .addCallback(SchoolDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SchoolDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.schoolDao())
                }
            }
        }

        suspend fun populateDatabase(dao: SchoolDao) {
            // 1. Settings
            dao.insertSettings(SchoolSettings())

            // 2. Classes
            val classes = listOf(
                SchoolClass("VI", "Class 6-8", "General"),
                SchoolClass("VII", "Class 6-8", "General"),
                SchoolClass("VIII", "Class 6-8", "General"),
                SchoolClass("IX", "Class 9-10", "General"),
                SchoolClass("X A", "Class 9-10", "General"),
                SchoolClass("X B", "Class 9-10", "General"),
                SchoolClass("XI A", "Class 11-12", "Arts"),
                SchoolClass("XI B", "Class 11-12", "Science"),
                SchoolClass("XII A", "Class 11-12", "Arts"),
                SchoolClass("XII B", "Class 11-12", "Science")
            )
            dao.insertClasses(classes)

            // 3. Periods
            val periods = listOf(
                Period("I", "07:55 - 08:30", 0),
                Period("II", "08:30 - 09:05", 1),
                Period("III", "09:05 - 09:40", 2),
                Period("IV", "09:40 - 10:15", 3),
                Period("V", "10:40 - 11:15", 4),
                Period("VI", "11:15 - 11:50", 5),
                Period("VII", "11:50 - 12:25", 6),
                Period("VIII", "12:25 - 01:00", 7)
            )
            dao.insertPeriods(periods)

            // 4. Subjects
            val subjects = listOf(
                "Free", "MATHS", "ENGLISH", "HINDI", "SANSKRIT", "SST", "SCIENCE",
                "ENVIRONMENT", "SPORTS", "HINDI LT.", "BIO/MATH", "HISTORY",
                "POL. SCIENCE", "CHEMISTRY", "PHYSICS", "BIO PRACTICAL",
                "CHE PRACTICAL", "PHY PRACTICAL", "AAZADI K BAD SWARNIM BHARAT 1",
                "AAZADI K BAD SWARNIM BHARAT 2", "RAJASTHAN STUDY", "LIFE SKILLS EDU.",
                "DIAGNOSTIC TEACHING", "LIBRARY", "ART EDUCATION", "HEALTH $ PHY. EDU.",
                "SUPW", "COMPUTER -IT"
            ).map { Subject(it) }
            dao.insertSubjects(subjects)

            // 5. Teachers
            val teachers = listOf(
                "None", "KISHOR KUMAR DAVE", "RUDA RAM REBARI", "PURKHARAM MEGHWAL",
                "JABRA RAM", "MAHESH CHANDRA LAKHARA", "RAVINDRA SARAN", "VIKRAM POONIA",
                "RAM BABU JATAV", "HARI RAM CHOUDHARY", "TEJPAL SINGH VISHNOI",
                "BHANWAR LAL DUDI", "BHANWAR LAL SAHU", "PANCHAN MAL", "KELI BISHNOI",
                "NEMEECHAND KHARWAL", "SUNITA KUMARI", "TEJA RAM", "HARCHAND RAM",
                "KHEMI DEVI", "THANA RAM", "RAJURAM BISHNOI", "BHANWAR LAL PET",
                "VP/RV", "MAHIPAL", "RAMESH KUMAR", "ANIL KUMAR", "REENA TYAGI",
                "DINESH KUMAR"
            ).map { Teacher(it) }
            dao.insertTeachers(teachers)

            // 6. Pre-populate default schedules (Parsed version of compactTimetableSource)
            val compactSource = listOf(
                // I
                CompactItem("I", "VI", "MATHS", "SUNITA KUMARI", "1-6"),
                CompactItem("I", "VII", "ENGLISH", "TEJA RAM", "1-6"),
                CompactItem("I", "VIII", "HINDI", "KELI BISHNOI", "1-6"),
                CompactItem("I", "IX", "SANSKRIT", "HARI RAM CHOUDHARY", "1-5"),
                CompactItem("I", "IX", "SUPW", "HARI RAM CHOUDHARY", "6"),
                CompactItem("I", "X A", "MATHS", "TEJPAL SINGH VISHNOI", "1-6"),
                CompactItem("I", "X B", "SCIENCE", "BHANWAR LAL DUDI", "1-6"),
                CompactItem("I", "XI A", "HINDI LT.", "BHANWAR LAL SAHU", "1-6"),
                CompactItem("I", "XI B", "BIO/MATH", "VP/RV", "1-6"),
                CompactItem("I", "XII A", "HISTORY", "MAHESH CHANDRA LAKHARA", "1-6"),
                CompactItem("I", "XII B", "PHYSICS", "JABRA RAM", "1-6"),
                // II
                CompactItem("II", "VI", "HINDI", "KELI BISHNOI", "1-6"),
                CompactItem("II", "VII", "SANSKRIT", "THANA RAM", "1-6"),
                CompactItem("II", "VIII", "MATHS", "SUNITA KUMARI", "1-6"),
                CompactItem("II", "IX", "SST", "RAJURAM BISHNOI", "1-6"),
                CompactItem("II", "X A", "MATHS", "TEJPAL SINGH VISHNOI", "1-2"),
                CompactItem("II", "X A", "RAJASTHAN STUDY", "TEJPAL SINGH VISHNOI", "3-4"),
                CompactItem("II", "X A", "HEALTH $ PHY. EDU.", "TEJPAL SINGH VISHNOI", "5-6"),
                CompactItem("II", "X B", "SCIENCE", "BHANWAR LAL DUDI", "1-4"),
                CompactItem("II", "X B", "COMPUTER -IT", "BHANWAR LAL DUDI", "5-6"),
                CompactItem("II", "XI A", "ENGLISH", "PANCHAN MAL", "1-6"),
                CompactItem("II", "XI B", "BIO/MATH", "VP/RV", "1-3"),
                CompactItem("II", "XI B", "AAZADI K BAD SWARNIM BHARAT 1", "RAMESH KUMAR", "4"),
                CompactItem("II", "XI B", "BIO PRACTICAL", "VIKRAM POONIA", "5-6"),
                CompactItem("II", "XII A", "HISTORY", "MAHESH CHANDRA LAKHARA", "1-5"),
                CompactItem("II", "XII A", "AAZADI K BAD SWARNIM BHARAT 2", "MAHESH CHANDRA LAKHARA", "6"),
                CompactItem("II", "XII B", "PHYSICS", "JABRA RAM", "1-2,4"),
                CompactItem("II", "XII B", "AAZADI K BAD SWARNIM BHARAT 2", "RAMESH KUMAR", "3"),
                CompactItem("II", "XII B", "PHY PRACTICAL", "JABRA RAM", "5-6"),
                // III
                CompactItem("III", "VI", "SCIENCE", "SUNITA KUMARI", "1-6"),
                CompactItem("III", "VII", "HINDI", "KELI BISHNOI", "1-6"),
                CompactItem("III", "VIII", "SST", "NEMEECHAND KHARWAL", "1-6"),
                CompactItem("III", "IX", "SCIENCE", "BHANWAR LAL DUDI", "1-6"),
                CompactItem("III", "X A", "ENGLISH", "PANCHAN MAL", "1-6"),
                CompactItem("III", "X B", "HINDI", "BHANWAR LAL SAHU", "1-6"),
                CompactItem("III", "XI A", "HISTORY", "MAHESH CHANDRA LAKHARA", "1-6"),
                CompactItem("III", "XI B", "CHEMISTRY", "RAM BABU JATAV", "1-6"),
                CompactItem("III", "XII A", "POL. SCIENCE", "PURKHARAM MEGHWAL", "1-6"),
                CompactItem("III", "XII B", "BIO/MATH", "VP/RV", "1-6"),
                // IV
                CompactItem("IV", "VI", "SST", "NEMEECHAND KHARWAL", "1-6"),
                CompactItem("IV", "VII", "MATHS", "SUNITA KUMARI", "1-6"),
                CompactItem("IV", "VIII", "ENGLISH", "TEJA RAM", "1-6"),
                CompactItem("IV", "IX", "MATHS", "TEJPAL SINGH VISHNOI", "1-6"),
                CompactItem("IV", "X A", "HINDI", "BHANWAR LAL SAHU", "1-6"),
                CompactItem("IV", "X B", "ENGLISH", "PANCHAN MAL", "1-6"),
                CompactItem("IV", "XI A", "POL. SCIENCE", "PURKHARAM MEGHWAL", "1-6"),
                CompactItem("IV", "XI B", "CHEMISTRY", "RAM BABU JATAV", "1-3"),
                CompactItem("IV", "XI B", "AAZADI K BAD SWARNIM BHARAT 1", "RAMESH KUMAR", "4"),
                CompactItem("IV", "XI B", "CHE PRACTICAL", "RAM BABU JATAV", "5-6"),
                CompactItem("IV", "XII A", "HINDI LT.", "RUDA RAM REBARI", "1-6"),
                CompactItem("IV", "XII B", "BIO/MATH", "VP/RV", "1-2,4"),
                CompactItem("IV", "XII B", "AAZADI K BAD SWARNIM BHARAT 2", "RAMESH KUMAR", "3"),
                CompactItem("IV", "XII B", "BIO PRACTICAL", "VIKRAM POONIA", "5-6"),
                // V
                CompactItem("V", "VI", "ENGLISH", "TEJA RAM", "1-6"),
                CompactItem("V", "VII", "SST", "NEMEECHAND KHARWAL", "1-6"),
                CompactItem("V", "VIII", "SCIENCE", "SUNITA KUMARI", "1-6"),
                CompactItem("V", "IX", "HINDI", "KELI BISHNOI", "1-6"),
                CompactItem("V", "X A", "SCIENCE", "BHANWAR LAL DUDI", "1-6"),
                CompactItem("V", "X B", "MATHS", "TEJPAL SINGH VISHNOI", "1-6"),
                CompactItem("V", "XI A", "HINDI", "HARI RAM CHOUDHARY", "1-6"),
                CompactItem("V", "XI B", "ENGLISH", "PANCHAN MAL", "1-3"),
                CompactItem("V", "XII A", "POL. SCIENCE", "PURKHARAM MEGHWAL", "1-5"),
                CompactItem("V", "XII A", "AAZADI K BAD SWARNIM BHARAT 2", "PURKHARAM MEGHWAL", "6"),
                CompactItem("V", "XII B", "HINDI", "RUDA RAM REBARI", "1-6"),
                // VI
                CompactItem("VI", "VI", "SANSKRIT", "THANA RAM", "1-5"),
                CompactItem("VI", "VI", "SUPW", "THANA RAM", "6"),
                CompactItem("VI", "VII", "SCIENCE", "SUNITA KUMARI", "1-6"),
                CompactItem("VI", "VIII", "ART EDUCATION", "TEJA RAM", "1-2"),
                CompactItem("VI", "VIII", "SUPW", "TEJA RAM", "3-4"),
                CompactItem("VI", "VIII", "HEALTH $ PHY. EDU.", "TEJA RAM", "5-6"),
                CompactItem("VI", "IX", "SCIENCE", "BHANWAR LAL DUDI", "1-4"),
                CompactItem("VI", "IX", "COMPUTER -IT", "BHANWAR LAL DUDI", "5-6"),
                CompactItem("VI", "X A", "SANSKRIT", "HARI RAM CHOUDHARY", "1-6"),
                CompactItem("VI", "X B", "MATHS", "TEJPAL SINGH VISHNOI", "1-2"),
                CompactItem("VI", "X B", "RAJASTHAN STUDY", "TEJPAL SINGH VISHNOI", "3-4"),
                CompactItem("VI", "X B", "HEALTH $ PHY. EDU.", "TEJPAL SINGH VISHNOI", "5-6"),
                CompactItem("VI", "XI A", "HINDI LT.", "BHANWAR LAL SAHU", "1-6"),
                CompactItem("VI", "XI B", "HINDI", "RUDA RAM REBARI", "1-3"),
                CompactItem("VI", "XI B", "LIFE SKILLS EDU.", "RUDA RAM REBARI", "4-6"),
                CompactItem("VI", "XII A", "ENGLISH", "PANCHAN MAL", "1-6"),
                CompactItem("VI", "XII B", "CHEMISTRY", "RAM BABU JATAV", "1-6"),
                // VII
                CompactItem("VII", "VI", "DIAGNOSTIC TEACHING", "KELI BISHNOI", "1-5"),
                CompactItem("VII", "VI", "LIBRARY", "RAJURAM BISHNOI", "6"),
                CompactItem("VII", "VII", "HEALTH $ PHY. EDU.", "BHANWAR LAL PET", "1-2"),
                CompactItem("VII", "VII", "SUPW", "BHANWAR LAL PET", "3-4"),
                CompactItem("VII", "VII", "ART EDUCATION", "BHANWAR LAL PET", "5-6"),
                CompactItem("VII", "VIII", "SANSKRIT", "THANA RAM", "1-6"),
                CompactItem("VII", "IX", "MATHS", "TEJPAL SINGH VISHNOI", "1-2"),
                CompactItem("VII", "IX", "RAJASTHAN STUDY", "TEJPAL SINGH VISHNOI", "3-4"),
                CompactItem("VII", "IX", "HEALTH $ PHY. EDU.", "TEJPAL SINGH VISHNOI", "5-6"),
                CompactItem("VII", "X A", "SCIENCE", "BHANWAR LAL DUDI", "1-4"),
                CompactItem("VII", "X A", "COMPUTER -IT", "BHANWAR LAL DUDI", "5-6"),
                CompactItem("VII", "X B", "SST", "NEMEECHAND KHARWAL", "1-6"),
                CompactItem("VII", "XI A", "POL. SCIENCE", "PURKHARAM MEGHWAL", "1-3"),
                CompactItem("VII", "XI A", "LIFE SKILLS EDU.", "PURKHARAM MEGHWAL", "4-6"),
                CompactItem("VII", "XI B", "PHYSICS", "JABRA RAM", "1-6"),
                CompactItem("VII", "XII A", "HINDI LT.", "RUDA RAM REBARI", "1-5"),
                CompactItem("VII", "XII A", "AAZADI K BAD SWARNIM BHARAT 2", "RUDA RAM REBARI", "6"),
                CompactItem("VII", "XII B", "CHEMISTRY", "RAM BABU JATAV", "1-2,4"),
                CompactItem("VII", "XII B", "AAZADI K BAD SWARNIM BHARAT 2", "RAMESH KUMAR", "3"),
                CompactItem("VII", "XII B", "CHE PRACTICAL", "RAM BABU JATAV", "5-6"),
                // VIII
                CompactItem("VIII", "VI", "HEALTH $ PHY. EDU.", "BHANWAR LAL PET", "1-2"),
                CompactItem("VIII", "VI", "SUPW", "BHANWAR LAL PET", "3-4"),
                CompactItem("VIII", "VI", "ART EDUCATION", "BHANWAR LAL PET", "5-6"),
                CompactItem("VIII", "VII", "DIAGNOSTIC TEACHING", "KELI BISHNOI", "1-5"),
                CompactItem("VIII", "VII", "LIBRARY", "RAJURAM BISHNOI", "6"),
                CompactItem("VIII", "VIII", "DIAGNOSTIC TEACHING", "MAHIPAL", "1-5"),
                CompactItem("VIII", "VIII", "LIBRARY", "MAHIPAL", "6"),
                CompactItem("VIII", "IX", "ENGLISH", "TEJA RAM", "1-6"),
                CompactItem("VIII", "X A", "SST", "NEMEECHAND KHARWAL", "1-6"),
                CompactItem("VIII", "X B", "SANSKRIT", "HARI RAM CHOUDHARY", "1-6"),
                CompactItem("VIII", "XI A", "HISTORY", "MAHESH CHANDRA LAKHARA", "1-3"),
                CompactItem("VIII", "XI A", "AAZADI K BAD SWARNIM BHARAT 1", "MAHESH CHANDRA LAKHARA", "4-6"),
                CompactItem("VIII", "XI B", "PHYSICS", "JABRA RAM", "1-3"),
                CompactItem("VIII", "XI B", "AAZADI K BAD SWARNIM BHARAT 1", "RAMESH KUMAR", "4"),
                CompactItem("VIII", "XI B", "PHY PRACTICAL", "JABRA RAM", "5-6"),
                CompactItem("VIII", "XII A", "HINDI", "BHANWAR LAL SAHU", "1-6"),
                CompactItem("VIII", "XII B", "ENGLISH", "PANCHAN MAL", "1-6")
            )

            val cellsToInsert = mutableListOf<TimetableCell>()
            compactSource.forEach { item ->
                val days = parseDays(item.daysStr)
                days.forEach { day ->
                    cellsToInsert.add(
                        TimetableCell(
                            periodName = item.period,
                            className = item.className,
                            dayOfWeek = day,
                            subjectName = item.subject,
                            teacherName = item.teacher
                        )
                    )
                }
            }
            dao.insertCells(cellsToInsert)
        }

        private fun parseDays(daysStr: String): List<Int> {
            val list = mutableListOf<Int>()
            val parts = daysStr.split(",")
            for (part in parts) {
                if (part.contains("-")) {
                    val split = part.split("-")
                    val start = split[0].trim().toIntOrNull() ?: continue
                    val end = split[1].trim().toIntOrNull() ?: continue
                    for (i in start..end) {
                        list.add(i)
                    }
                } else {
                    val day = part.trim().toIntOrNull() ?: continue
                    list.add(day)
                }
            }
            return list
        }
    }

    private data class CompactItem(
        val period: String,
        val className: String,
        val subject: String,
        val teacher: String,
        val daysStr: String
    )
}
