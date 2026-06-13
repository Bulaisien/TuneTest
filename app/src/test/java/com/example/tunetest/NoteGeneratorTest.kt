package com.example.tunetest

import com.example.tunetest.game.NoteGenerator
import com.example.tunetest.musictheory.MTConsts
import org.junit.Test

import org.junit.Assert.*

class NoteGeneratorTest {

    @Test
    fun canGenerateValidNote() {
        repeat(100) {
            val note = NoteGenerator.generate()
            assertTrue(note.midiNumber in MTConsts.MIDI_TO_NAME.keys)
            assertEquals(MTConsts.MIDI_TO_NAME[note.midiNumber], note.displayName)
        }
    }
}
