package com.example.tunetest.audio.pcm

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.tunetest.audio.pcm.PcmAudioConfig.SAMPLE_RATE
import com.example.tunetest.audio.PlaybackRunner
import java.util.concurrent.Executors

class AudioTrackPcmPlayer(
    private val trackFactory: PcmTrackFactory = AudioTrackFactory(),
    private val playbackRunner: PlaybackRunner = ThreadPlaybackRunner()
) : PcmPlayer {
    private var currentTrack: PcmTrack? = null

    override fun play(samples: ShortArray) {
        playbackRunner.run {
            stopCurrentTrack()
            playSamples(samples)
        }
    }

    override fun stop() {
        playbackRunner.run {
            stopCurrentTrack()
        }
    }

    private fun stopCurrentTrack() {
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
        val track = trackFactory.create(samples)
        currentTrack = track

        track.write(samples, 0, samples.size)
        track.play()
    }
}

class AudioTrackFactory : PcmTrackFactory {
    override fun create(samples: ShortArray): PcmTrack {
        val audioTrack = AudioTrack.Builder()
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

        return AndroidPcmTrack(audioTrack)
    }
}

class AndroidPcmTrack(
    private val audioTrack: AudioTrack
) : PcmTrack {
    override fun write(samples: ShortArray, offset: Int, size: Int) {
        audioTrack.write(samples, offset, size)
    }

    override fun play() {
        audioTrack.play()
    }

    override fun pause() {
        audioTrack.pause()
    }

    override fun flush() {
        audioTrack.flush()
    }

    override fun release() {
        audioTrack.release()
    }
}

class ThreadPlaybackRunner : PlaybackRunner {
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SynthAudioEngine")
    }

    override fun run(block: () -> Unit) {
        executor.execute(block)
    }
}
