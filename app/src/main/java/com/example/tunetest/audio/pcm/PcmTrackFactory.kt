package com.example.tunetest.audio.pcm

interface PcmTrackFactory {
    fun create(samples: ShortArray): PcmTrack
}
