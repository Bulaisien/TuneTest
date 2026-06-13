package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.MTConsts.NOTE_LIST
import com.example.tunetest.settings.MusicTheorySettings

object SingleNoteGenerator : QuestionGenerator {
    override fun generate(settings: MusicTheorySettings): Question {
        val correctedSettings = settings.corrected()
        val note = NoteGenerator.generate(correctedSettings)
        val prompt = AudioPrompt.SingleNote(note)
        val answer = NOTE_LIST[note.midiNumber % NOTE_LIST.size]
        return Question(
            prompt,
            correctedSettings.noteChoices.indexOf(answer)
        )
    }
}
