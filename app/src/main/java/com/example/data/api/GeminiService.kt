package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiAiAssistant {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getSchedulingAdvice(
        teachers: List<Teacher>,
        subjects: List<Subject>,
        classes: List<SchoolClass>,
        periods: List<Period>,
        currentCells: List<TimetableCell>,
        questionType: String, // "resolve_conflicts", "substitute_teacher", "optimize_workload"
        additionalInfo: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API key not configured. Please add your GEMINI_API_KEY to the AI Studio Secrets panel."
        }

        // Build a concise textual representation of the current school schedule
        val teachersStr = teachers.joinToString { it.name }
        val subjectsStr = subjects.joinToString { it.name }
        val classesStr = classes.joinToString { it.name }
        val periodStr = periods.joinToString { "${it.name}(${it.time})" }
        
        // Find current duplicate teacher bookings (conflicts)
        val conflicts = mutableListOf<String>()
        val bookedMap = mutableMapOf<String, String>() // Key: "periodName-dayOfWeek-teacherName", Value: "className"
        currentCells.forEach { cell ->
            if (cell.teacherName != "None" && cell.teacherName.isNotEmpty()) {
                val key = "${cell.periodName}-${cell.dayOfWeek}-${cell.teacherName}"
                if (bookedMap.containsKey(key)) {
                    conflicts.add("Teacher '${cell.teacherName}' is double-booked on day ${cell.dayOfWeek} (1=Mon, 6=Sat) during Period '${cell.periodName}' for both Class '${cell.className}' and Class '${bookedMap[key]}'.")
                } else {
                    bookedMap[key] = cell.className
                }
            }
        }

        val prompt = when(questionType) {
            "resolve_conflicts" -> """
                You are an Expert School Timetable AI Assistant.
                Here is the current school data:
                - Classes: $classesStr
                - Periods: $periodStr
                - Subjects: $subjectsStr
                - Teachers Available: $teachersStr
                
                We have detected the following scheduling conflicts:
                ${if (conflicts.isEmpty()) "None. Everything is clean!" else conflicts.joinToString("\n")}
                
                Please provide concrete conflict resolution recommendations. For each conflict, suggest which teacher or subject could be substituted to resolve the clash. Be highly professional and brief.
            """.trimIndent()
            
            "substitute_teacher" -> """
                You are an Expert School Timetable AI Assistant.
                Here is the current school data:
                - Teachers Available: $teachersStr
                - Subjects: $subjectsStr
                
                The principal needs to replace or find a substitute for teacher: '$additionalInfo'
                Explain who would be the best replacement or alternative teacher for their classes, considering subject matches and typical school requirements. Provide clear, direct options.
            """.trimIndent()
            
            "optimize_workload" -> """
                You are an Expert School Timetable AI Assistant.
                Here is the current school workload layout:
                - Teachers: $teachersStr
                - Current schedules: ${currentCells.size} active periods assigned.
                
                Suggest standard Material design policies and workload distribution metrics for maximum efficiency. Recommend how the principal can balance workloads across senior secondary, secondary, and primary teachers.
            """.trimIndent()
            
            else -> "Give general tips for managing a school timetable efficiently."
        }

        // Clean up the prompt strings to be JSON safe
        val escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")

        val jsonRequest = """
            {
              "contents": [{
                "parts": [{
                  "text": "$escapedPrompt"
                }]
              }],
              "systemInstruction": {
                "parts": [{
                  "text": "You are a professional enterprise scheduler assistant for Indian schools, answering with clear bullet points."
                }]
              }
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Failed to contact Gemini API. Error code: ${response.code}"
                }
                val responseBody = response.body?.string() ?: ""
                
                // Extract "text" from response manual parser (bulletproof, robust, dependency-free)
                val textMarker = "\"text\":"
                var searchIndex = 0
                val resultText = StringBuilder()
                
                while (true) {
                    val markerIndex = responseBody.indexOf(textMarker, searchIndex)
                    if (markerIndex == -1) break
                    
                    // Locate start of string quote after marker
                    val quoteStartIndex = responseBody.indexOf("\"", markerIndex + textMarker.length)
                    if (quoteStartIndex == -1) break
                    
                    // Locate closing quote, handling escaped quotes
                    var quoteEndIndex = quoteStartIndex + 1
                    while (quoteEndIndex < responseBody.length) {
                        if (responseBody[quoteEndIndex] == '\"' && responseBody[quoteEndIndex - 1] != '\\') {
                            break
                        }
                        quoteEndIndex++
                    }
                    
                    if (quoteEndIndex < responseBody.length) {
                        val extracted = responseBody.substring(quoteStartIndex + 1, quoteEndIndex)
                        val unescaped = extracted
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                        resultText.append(unescaped)
                    }
                    searchIndex = quoteEndIndex + 1
                }
                
                val finalStr = resultText.toString().trim()
                if (finalStr.isNotEmpty()) {
                    finalStr
                } else {
                    "No response text received from Gemini API."
                }
            }
        } catch (e: Exception) {
            "Failed to contact Gemini API: ${e.message}. Please check your internet connection or API Key."
        }
    }
}
