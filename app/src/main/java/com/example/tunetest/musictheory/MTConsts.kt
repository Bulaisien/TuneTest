package com.example.tunetest.musictheory

object MTConsts {

    val NOTE_LIST = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )

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