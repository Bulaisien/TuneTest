package com.example.tunetest.audio.pcm

interface PcmTrack {
    fun write(samples: ShortArray, offset: Int, size: Int)
    fun play()
    fun pause()
    fun flush()
    fun release()
}
