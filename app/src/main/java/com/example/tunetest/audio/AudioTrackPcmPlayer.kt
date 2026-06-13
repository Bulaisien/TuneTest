package com.example.tunetest.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.tunetest.audio.PcmAudioConfig.SAMPLE_RATE
import kotlin.concurrent.thread

class AudioTrackPcmPlayer : PcmPlayer {
    private var currentTrack: AudioTrack? = null

    override fun play(samples: ShortArray) {
        stop()
        thread(name = "SynthAudioEngine") {
            playSamples(samples)
        }
    }

    override fun stop() {
        currentTrack?.let {
            try {
                it.pause()
                it.flush()
                it.release()
            } catch (_: IllegalStateException) {
                it.release()
            }
        }

        currentTrack = null
    }

    private fun playSamples(samples: ShortArray) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        currentTrack = track

        track.write(samples, 0, samples.size)
        track.play()
    }
}
