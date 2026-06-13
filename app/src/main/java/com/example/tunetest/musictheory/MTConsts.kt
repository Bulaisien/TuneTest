package com.example.tunetest.musictheory

object MTConsts {

    // These lists must be in order because other parts depend on that fact.
    val NOTE_LIST = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )
    val INTERVAL_LIST = listOf(
        "Unison", "Minor 2nd", "Major 2nd", "Minor 3rd",
        "Major 3rd", "Perfect 4th", "Tritone", "Perfect 5th",
        "Minor 6th", "Major 6th", "Minor 7th", "Major 7th", "Octave"
    )
    val TRIAD_QUALITY_LIST = TriadQuality.entries.map {
        it.name.lowercase().replaceFirstChar(Char::uppercase)
    }

    const val FIRST_MIDI_NUMBER = 60
    const val LAST_MIDI_NUMBER = 71
    val MIDI_TO_NAME: Map<Int, String> =
        (FIRST_MIDI_NUMBER..LAST_MIDI_NUMBER).associateWith { midiToName(it) }

    fun midiToName(midiNumber: Int): String {
        val noteName = NOTE_LIST[midiNumber % NOTE_LIST.size]
        val octave = (midiNumber / NOTE_LIST.size) - 1
        return "$noteName$octave"
    }
}