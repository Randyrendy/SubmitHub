package com.submithub.app.data.entity

data class SubmissionEntity(
    val id: String = "",
    val codeId: String,
    val courseTitle: String,
    val assignmentTitle: String,
    val iconKey: String,
    val studentId: String,
    val studentName: String,
    val fileName: String,
    val fileUri: String,
    val submittedAt: Long,
    val pageCount: Int = 0,
    val isConfirmed: Boolean = false,
    // Grading (all null until a lecturer grades the submission)
    val correctnessScore: Int? = null,
    val codeQualityScore: Int? = null,
    val documentationScore: Int? = null,
    val testingScore: Int? = null,
    val feedback: String? = null,
    val gradedAt: Long? = null
) {
    val isGraded: Boolean
        get() = gradedAt != null

    val totalScore: Int?
        get() {
            if (correctnessScore == null || codeQualityScore == null ||
                documentationScore == null || testingScore == null
            ) return null
            return correctnessScore + codeQualityScore + documentationScore + testingScore
        }
}