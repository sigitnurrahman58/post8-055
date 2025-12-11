package com.git.firebasetodolist

data class Book(
    var id: String? = null,
    var title: String? = "",
    var release: String? = "",
    var description: String? = "",
    var done: Boolean = false,
    var date: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
