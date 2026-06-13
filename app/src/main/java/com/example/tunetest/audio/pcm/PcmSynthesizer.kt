package com.example.tunetest.audio.pcm

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.settings.DurationSettings

interface PcmSynthesizer {
    fun render(
        prompt: AudioPrompt,
        durationSettings: DurationSettings = DurationSettings()
    ): ShortArray
}
