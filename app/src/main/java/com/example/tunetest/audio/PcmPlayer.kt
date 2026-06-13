package com.example.tunetest.audio

interface PcmPlayer {
    fun play(samples: ShortArray)
    fun stop()
}