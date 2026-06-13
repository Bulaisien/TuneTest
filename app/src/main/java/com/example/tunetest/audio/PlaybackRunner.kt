package com.example.tunetest.audio

interface PlaybackRunner {
    fun run(block: () -> Unit)
}
