package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.musictheory.MTConsts.NOTE_LIST

object SingleNoteGenerator : QuestionGenerator {
    override fun generate(): Question {
        val note = NoteGenerator.generate()
        val prompt = AudioPrompt.SingleNote(note)
        return Question(
            prompt,
            note.midiNumber % NOTE_LIST.size
        )
    }
}
