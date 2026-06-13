package com.example.tunetest.audio

import com.example.tunetest.audio.PcmAudioConfig.MAX_AMPLITUDE
import com.example.tunetest.audio.PcmAudioConfig.SAMPLE_RATE
import com.example.tunetest.audio.DurationConfig.ATTACK_SECONDS
import com.example.tunetest.audio.DurationConfig.INTERVAL_SILENCE_SECONDS
import com.example.tunetest.audio.DurationConfig.INTERVAL_TONE_SECONDS
import com.example.tunetest.audio.DurationConfig.RELEASE_SECONDS
import com.example.tunetest.audio.DurationConfig.SINGLE_NOTE_SECONDS
import com.example.tunetest.audio.DurationConfig.TRIAD_SECONDS
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class DefaultPcmSynthesizer : PcmSynthesizer {
    override fun render(prompt: AudioPrompt) : ShortArray {
        val samples = when (prompt) {
            is AudioPrompt.SingleNote -> {
                tone(prompt.note.midiNumber, seconds = SINGLE_NOTE_SECONDS)
            }

            is AudioPrompt.Interval -> {
                sequence(
                    tone(prompt.root.midiNumber, seconds = INTERVAL_TONE_SECONDS),
                    silence(seconds = INTERVAL_SILENCE_SECONDS),
                    tone(prompt.root.midiNumber + prompt.semitones, seconds = INTERVAL_TONE_SECONDS)
                )
            }

            is AudioPrompt.Triad -> {
                val notes = prompt.quality.semitones.map { prompt.root.midiNumber + it }
                chord(notes, seconds = TRIAD_SECONDS)
            }
        }

        return samples
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
        val attackSamples = (SAMPLE_RATE * ATTACK_SECONDS).toInt()
        val releaseSamples = (SAMPLE_RATE * RELEASE_SECONDS).toInt()

        val attack = (index.toDouble() / attackSamples).coerceIn(0.0, 1.0)
        val release = ((sampleCount - index).toDouble() / releaseSamples).coerceIn(0.0, 1.0)

        return minOf(attack, release)
    }

    private fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69) / 12.0)
    }
}
