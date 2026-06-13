package com.example.tunetest.settings

import com.example.tunetest.musictheory.MTConsts
import com.example.tunetest.musictheory.TriadQuality

data class MusicTheorySettings(
    val firstMidiNumber: Int = 54,
    val lastMidiNumber: Int = 77,
    val enabledNoteNames: Set<String> = MTConsts.NOTE_LIST.toSet(),
    val enabledIntervals: Set<Int> = MTConsts.INTERVAL_LIST.indices.toSet(),
    val enabledTriadQualities: Set<TriadQuality> = TriadQuality.entries.toSet()
) {
    val noteChoices: List<String>
        get() = MTConsts.NOTE_LIST.filter { it in enabledNoteNames }

    val intervalChoices: List<String>
        get() = enabledIntervals.sorted().map { MTConsts.INTERVAL_LIST[it] }

    val triadChoices: List<String>
        get() = enabledTriadQualities.sortedBy { it.ordinal }.map { it.displayName }

    fun corrected(): MusicTheorySettings {
        val orderedRange = if (firstMidiNumber <= lastMidiNumber) {
            firstMidiNumber to lastMidiNumber
        } else {
            lastMidiNumber to firstMidiNumber
        }
        val validNoteNames = enabledNoteNames.filter { it in MTConsts.NOTE_LIST }.toSet()
        val validIntervals = enabledIntervals.filter { it in MTConsts.INTERVAL_LIST.indices }.toSet()
        val validTriadQualities = enabledTriadQualities.filter { it in TriadQuality.entries }.toSet()

        return copy(
            firstMidiNumber = orderedRange.first.coerceIn(MIN_MIDI_NUMBER, MAX_MIDI_NUMBER),
            lastMidiNumber = orderedRange.second.coerceIn(MIN_MIDI_NUMBER, MAX_MIDI_NUMBER),
            enabledNoteNames = validNoteNames.ifEmpty { MTConsts.NOTE_LIST.toSet() },
            enabledIntervals = validIntervals.ifEmpty { setOf(0) },
            enabledTriadQualities = validTriadQualities.ifEmpty { setOf(TriadQuality.MAJOR) }
        )
    }

    companion object {
        const val MIN_MIDI_NUMBER = 21
        const val MAX_MIDI_NUMBER = 108
    }
}

val TriadQuality.displayName: String
    get() = name.lowercase().replaceFirstChar(Char::uppercase)
