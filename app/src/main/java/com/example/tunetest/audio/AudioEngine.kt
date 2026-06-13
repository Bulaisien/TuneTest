package com.example.tunetest.audio

import com.example.tunetest.settings.DurationSettings

interface AudioEngine {
    fun play(prompt: AudioPrompt, durationSettings: DurationSettings = DurationSettings())
    fun stop()
}
