package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.TriadQuality
import com.example.tunetest.settings.MusicTheorySettings

object TriadGenerator : QuestionGenerator {
    override fun generate(settings: MusicTheorySettings): Question {
        val correctedSettings = settings.corrected()
        val rootNote = NoteGenerator.generate(correctedSettings)
        val quality = correctedSettings.enabledTriadQualities.random()
        val prompt = AudioPrompt.Triad(rootNote, quality)
        val choices = correctedSettings.enabledTriadQualities.sortedBy { it.ordinal }
        return Question(prompt, choices.indexOf(quality))
    }
}
