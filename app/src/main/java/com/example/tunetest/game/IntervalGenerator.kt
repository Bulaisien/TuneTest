package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import kotlin.random.Random

object IntervalGenerator : QuestionGenerator {
    override fun generate(): Question {
        val rootNote = NoteGenerator.generate()
        val semitones = Random.nextInt(13)
        return Question(
            AudioPrompt.Interval(rootNote, semitones),
            semitones
        )
    }
}
