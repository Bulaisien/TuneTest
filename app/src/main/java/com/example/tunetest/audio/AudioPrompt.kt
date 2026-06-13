package com.example.tunetest.audio

import com.example.tunetest.musictheory.Note
import com.example.tunetest.musictheory.TriadQuality

sealed class AudioPrompt {
    data class SingleNote(val note: Note) : AudioPrompt()
    data class Interval(val root: Note, val semitones: Int) : AudioPrompt()
    data class Triad(val root: Note, val quality: TriadQuality) : AudioPrompt()
}
