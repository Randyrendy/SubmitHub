package com.submithub.app.util

import com.submithub.app.R

object IconCatalog {
    data class IconOption(val key: String, val resId: Int, val label: String)

    val options = listOf(
        IconOption("assignment", R.drawable.ic_assignment, "Assignment"),
        IconOption("book", R.drawable.ic_book, "Coursework"),
        IconOption("lab", R.drawable.ic_lab, "Lab Report"),
        IconOption("calendar", R.drawable.ic_calendar, "Take-home"),
        IconOption("upload", R.drawable.ic_upload, "Project"),
        IconOption("school", R.drawable.ic_school, "Term Paper")
    )

    fun resFor(key: String): Int = options.firstOrNull { it.key == key }?.resId ?: R.drawable.ic_assignment
}
