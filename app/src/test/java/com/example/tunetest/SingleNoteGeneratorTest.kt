package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.game.SingleNoteGenerator
import com.example.tunetest.musictheory.MTConsts.NOTE_LIST
import org.junit.Test

import org.junit.Assert.*

class SingleNoteGeneratorTest {
    @Test
    fun canGenerateSingleNoteAudioPrompt() {
        repeat(100) {
            val question = SingleNoteGenerator.generate()
            assert(question.audioPrompt is AudioPrompt.SingleNote)
        }
    }

    @Test
    fun promptIsCohesiveWithCorrectAnswer() {
        repeat(100) {
            val question = SingleNoteGenerator.generate()
            if (question.audioPrompt !is AudioPrompt.SingleNote) fail("Audio prompt is not SingleNote")
            assertEquals(
                (question.audioPrompt as AudioPrompt.SingleNote).note.displayName.takeWhile { c -> c in "ABCDEFG#"},
                NOTE_LIST[question.correctAnswerIndex]
            )
        }
    }
}
