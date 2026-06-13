package com.example.tunetest.settings

data class GameSettings(
    val duration: DurationSettings = DurationSettings(),
    val musicTheory: MusicTheorySettings = MusicTheorySettings()
)
