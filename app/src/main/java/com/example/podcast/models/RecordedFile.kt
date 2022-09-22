package com.example.podcast.models

import java.io.File

data class RecordedFile(
    val file: File,
    val name: String,
    val duration: String?,
    val lastModified: Long,
)
