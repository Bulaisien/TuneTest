package com.example.tunetest.audio

interface AudioEngine {
    fun play(prompt: AudioPrompt)
    fun stop()
}