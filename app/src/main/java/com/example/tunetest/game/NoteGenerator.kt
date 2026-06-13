package com.example.tunetest.game

import com.example.tunetest.musictheory.MTConsts.FIRST_MIDI_NUMBER
import com.example.tunetest.musictheory.MTConsts.LAST_MIDI_NUMBER
import com.example.tunetest.musictheory.MTConsts.NOTE_LIST
import com.example.tunetest.musictheory.MTConsts.midiToName
import com.example.tunetest.musictheory.Note
import kotlin.random.Random

object NoteGenerator {
    fun generate(): Note {
        val midiNumber = Random.nextInt(FIRST_MIDI_NUMBER, LAST_MIDI_NUMBER + 1)
        return Note(midiNumber, midiToName(midiNumber))
    }
}
