package com.example.tunetest.audio.pcm

import com.example.tunetest.audio.AudioPrompt

interface PcmSynthesizer {
    fun render(prompt: AudioPrompt): ShortArray
}