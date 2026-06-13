package com.example.tunetest.game

import com.example.tunetest.settings.MusicTheorySettings

enum class GameMode(
    val qGen: QuestionGenerator
) {
    SINGLE_NOTE(SingleNoteGenerator),
    INTERVAL(IntervalGenerator),
    TRIAD(TriadGenerator);

    fun choices(settings: MusicTheorySettings): List<String> {
        return when (this) {
            SINGLE_NOTE -> settings.noteChoices
            INTERVAL -> settings.intervalChoices
            TRIAD -> settings.triadChoices
        }
    }
}
