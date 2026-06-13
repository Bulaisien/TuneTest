package com.example.tunetest.audio.pcm

interface PcmPlayer {
    fun play(samples: ShortArray)
    fun stop()
}