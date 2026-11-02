package com.submithub.app.data

data class AnalyticsData(
    val totalSubmissions: Int,
    val averageGrade: Double?,
    val gradedRatePercent: Int,
    val lateRatePercent: Int,
    val gradeDistribution: List<Pair<String, Int>>,
    val submissionsByCourse: List<Pair<String, Int>>
)