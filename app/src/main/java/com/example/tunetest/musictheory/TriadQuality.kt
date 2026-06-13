package com.example.tunetest.musictheory

enum class TriadQuality(name: String, val semitones: List<Int>) {
    MAJOR("Major", listOf(0,4,7)),
    MINOR("Minor", listOf(0,3,7)),
    DIMINISHED("Diminished",listOf(0,3,6)),
    AUGMENTED("Augmented", listOf(0,4,8)),
}
