package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.MTConsts.INTERVAL_LIST
import com.example.tunetest.settings.MusicTheorySettings

object IntervalGenerator : QuestionGenerator {
    override fun generate(settings: MusicTheorySettings): Question {
        val correctedSettings = settings.corrected()
        val rootNote = NoteGenerator.generate(correctedSettings)
        val semitones = correctedSettings.enabledIntervals.random()
        return Question(
            AudioPrompt.Interval(rootNote, semitones),
            correctedSettings.intervalChoices.indexOf(INTERVAL_LIST[semitones])
        )
    }
}
