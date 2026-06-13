package com.example.tunetest.game

import com.example.tunetest.settings.MusicTheorySettings

interface QuestionGenerator {
    fun generate(settings: MusicTheorySettings = MusicTheorySettings()): Question
}
