package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.TriadQuality

object TriadGenerator : QuestionGenerator {
    override fun generate(): Question {
        val rootNote = NoteGenerator.generate()
        val quality = TriadQuality.entries.toTypedArray().random()
        val prompt = AudioPrompt.Triad(rootNote, quality)
        return Question(prompt, quality.ordinal)
    }
}
