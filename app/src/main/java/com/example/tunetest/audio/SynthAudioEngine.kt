package com.example.tunetest.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class SynthAudioEngine : AudioEngine {
    private var currentTrack: AudioTrack? = null

    override fun play(prompt: AudioPrompt) {
        stop()

        thread(name = "SynthAudioEngine") {
            val samples = when (prompt) {
                is AudioPrompt.SingleNote -> {
                    tone(prompt.note.midiNumber, seconds = 1.0)
                }

                is AudioPrompt.Interval -> {
                    sequence(
                        tone(prompt.root.midiNumber, seconds = 0.7),
                        silence(seconds = 0.12),
                        tone(prompt.root.midiNumber + prompt.semitones, seconds = 0.7)
                    )
                }

                is AudioPrompt.Triad -> {
                    val notes = prompt.quality.semitones.map { prompt.root.midiNumber + it }
                    chord(notes, seconds = 1.0)
                }
            }

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

    private fun tone(midi: Int, seconds: Double): ShortArray {
        val frequency = midiToFrequency(midi)
        val sampleCount = (SAMPLE_RATE * seconds).toInt()

        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val wave = sin(2.0 * PI * frequency * time)
            val envelope = envelope(index, sampleCount)

            (wave * envelope * MAX_AMPLITUDE).toInt().toShort()
        }
    }

    private fun chord(midis: List<Int>, seconds: Double): ShortArray {
        val frequencies = midis.map(::midiToFrequency)
        val sampleCount = (SAMPLE_RATE * seconds).toInt()

        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE

            val mixedWave = frequencies
                .sumOf { frequency ->
                    sin(2.0 * PI * frequency * time)
                } / frequencies.size

            val envelope = envelope(index, sampleCount)

            (mixedWave * envelope * MAX_AMPLITUDE).toInt().toShort()
        }
    }

    private fun silence(seconds: Double): ShortArray {
        return ShortArray((SAMPLE_RATE * seconds).toInt())
    }

    private fun sequence(vararg chunks: ShortArray): ShortArray {
        val totalSize = chunks.sumOf { it.size }
        val result = ShortArray(totalSize)

        var offset = 0

        chunks.forEach { chunk ->
            chunk.copyInto(
                destination = result,
                destinationOffset = offset
            )

            offset += chunk.size
        }

        return result
    }

    private fun envelope(index: Int, sampleCount: Int): Double {
        val attackSamples = (SAMPLE_RATE * 0.015).toInt()
        val releaseSamples = (SAMPLE_RATE * 0.05).toInt()

        val attack = (index.toDouble() / attackSamples).coerceIn(0.0, 1.0)
        val release = ((sampleCount - index).toDouble() / releaseSamples).coerceIn(0.0, 1.0)

        return minOf(attack, release)
    }

    private fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69) / 12.0)
    }

    companion object {
        private const val SAMPLE_RATE = 44_100
        private const val MAX_AMPLITUDE = 12_000.0
    }
}