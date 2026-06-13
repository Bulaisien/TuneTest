package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.game.TriadGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class TriadGeneratorTest {
    @Test
    fun canGenerateTriadAudioPrompt() {
        repeat(100) {
            val question = TriadGenerator.generate()
            assert(question.audioPrompt is AudioPrompt.Triad)
        }
    }

    @Test
    fun promptIsCohesiveWithCorrectAnswer() {
        repeat(100) {
            val question = TriadGenerator.generate()
            if (question.audioPrompt !is AudioPrompt.Triad) fail("Audio prompt is not Triad")
            assertEquals(
                (question.audioPrompt as AudioPrompt.Triad).quality.ordinal,
                question.correctAnswerIndex
            )
        }
    }
}