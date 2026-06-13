package com.example.tunetest.settings

data class DurationSettings(
    val singleNoteSeconds: Double = 1.5,
    val intervalToneSeconds: Double = 1.0,
    val intervalSilenceSeconds: Double = 0.2,
    val triadSeconds: Double = 1.5,
    val attackSeconds: Double = 0.015,
    val releaseSeconds: Double = 0.05
)
