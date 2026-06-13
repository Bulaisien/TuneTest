package com.example.tunetest.musictheory

enum class TriadQuality(val semitones: List<Int>) {
    MAJOR(listOf(0,4,7)),
    MINOR(listOf(0,3,7)),
    DIMINISHED(listOf(0,3,6)),
    AUGMENTED(listOf(0,4,8)),
}
