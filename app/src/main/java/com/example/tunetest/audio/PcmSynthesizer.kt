package com.example.tunetest.audio

interface PcmSynthesizer {
    fun render(prompt: AudioPrompt): ShortArray
}