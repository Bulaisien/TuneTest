package com.example.tunetest.game

import com.example.tunetest.musictheory.MTConsts.NOTE_LIST
import com.example.tunetest.musictheory.MTConsts.midiToName
import com.example.tunetest.musictheory.Note
import com.example.tunetest.settings.MusicTheorySettings
import kotlin.random.Random

object NoteGenerator {
    fun generate(settings: MusicTheorySettings = MusicTheorySettings()): Note {
        val correctedSettings = settings.corrected()
        val candidates = (correctedSettings.firstMidiNumber..correctedSettings.lastMidiNumber)
            .filter { midiNumber ->
                NOTE_LIST[midiNumber % NOTE_LIST.size] in correctedSettings.enabledNoteNames
            }
        val midiNumber = candidates.randomOrNull()
            ?: Random.nextInt(correctedSettings.firstMidiNumber, correctedSettings.lastMidiNumber + 1)
        return Note(midiNumber, midiToName(midiNumber))
    }
}
