package com.example.tunetest.musictheory

object MTConsts {

    val NOTE_LIST = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )
    val INTERVAL_LIST = listOf(
        "Unison", "Minor 2nd", "Major 2nd", "Minor 3rd",
        "Major 3rd", "Perfect 4th", "Tritone", "Perfect 5th",
        "Minor 6th", "Major 6th", "Minor 7th", "Major 7th", "Octave"
    )
    val TRIAD_QUALITY_LIST = TriadQuality.entries.map { q -> q.name }

    const val FIRST_MIDI_NUMBER = 60
    const val LAST_MIDI_NUMBER = 71

    val MIDI_TO_NAME = mapOf(
        60 to "C4",
        61 to "C#4",
        62 to "D4",
        63 to "D#4",
        64 to "E4",
        65 to "F4",
        66 to "F#4",
        67 to "G4",
        68 to "G#4",
        69 to "A4",
        70 to "A#4",
        71 to "B4",
    )
}