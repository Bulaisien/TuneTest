package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.game.IntervalGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class IntervalGeneratorTest {
    @Test
    fun canGenerateIntervalAudioPrompt() {
        repeat(100) {
            val question = IntervalGenerator.generate()
            assert(question.audioPrompt is AudioPrompt.Interval)
        }
    }

    @Test
    fun promptIsCohesiveWithCorrectAnswer() {
        repeat(100) {
            val question = IntervalGenerator.generate()
            if (question.audioPrompt !is AudioPrompt.Interval) fail("Audio prompt is not Interval")
            assertEquals(
                (question.audioPrompt as AudioPrompt.Interval).semitones,
                question.correctAnswerIndex
            )
        }
    }
}