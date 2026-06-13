package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.MTConsts.INTERVAL_LIST

object IntervalGenerator : QuestionGenerator {
    override fun generate(): Question {
        val rootNote = NoteGenerator.generate()
        val semitones = INTERVAL_LIST.indices.random()
        return Question(
            AudioPrompt.Interval(rootNote, semitones),
            semitones
        )
    }
}
