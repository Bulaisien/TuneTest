package com.example.tunetest.audio.pcm

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.audio.pcm.PcmAudioConfig.MAX_AMPLITUDE
import com.example.tunetest.audio.pcm.PcmAudioConfig.SAMPLE_RATE
import com.example.tunetest.settings.DurationSettings
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class DefaultPcmSynthesizer : PcmSynthesizer {
    override fun render(
        prompt: AudioPrompt,
        durationSettings: DurationSettings
    ) : ShortArray {
        val samples = when (prompt) {
            is AudioPrompt.SingleNote -> {
                tone(
                    midi = prompt.note.midiNumber,
                    seconds = durationSettings.singleNoteSeconds,
                    durationSettings = durationSettings
                )
            }

            is AudioPrompt.Interval -> {
                sequence(
                    tone(
                        midi = prompt.root.midiNumber,
                        seconds = durationSettings.intervalToneSeconds,
                        durationSettings = durationSettings
                    ),
                    silence(seconds = durationSettings.intervalSilenceSeconds),
                    tone(
                        midi = prompt.root.midiNumber + prompt.semitones,
                        seconds = durationSettings.intervalToneSeconds,
                        durationSettings = durationSettings
                    )
                )
            }

            is AudioPrompt.Triad -> {
                val notes = prompt.quality.semitones.map { prompt.root.midiNumber + it }
                chord(
                    midis = notes,
                    seconds = durationSettings.triadSeconds,
                    durationSettings = durationSettings
                )
            }
        }

        return samples
    }

    private fun tone(
        midi: Int,
        seconds: Double,
        durationSettings: DurationSettings
    ): ShortArray {
        val frequency = midiToFrequency(midi)
        val sampleCount = (SAMPLE_RATE * seconds).toInt()

        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val wave = sin(2.0 * PI * frequency * time)
            val envelope = envelope(index, sampleCount, durationSettings)

            (wave * envelope * MAX_AMPLITUDE).toInt().toShort()
        }
    }

    private fun chord(
        midis: List<Int>,
        seconds: Double,
        durationSettings: DurationSettings
    ): ShortArray {
        val frequencies = midis.map(::midiToFrequency)
        val sampleCount = (SAMPLE_RATE * seconds).toInt()

        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE

            val mixedWave = frequencies
                .sumOf { frequency ->
                    sin(2.0 * PI * frequency * time)
                } / frequencies.size

            val envelope = envelope(index, sampleCount, durationSettings)

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

    private fun envelope(
        index: Int,
        sampleCount: Int,
        durationSettings: DurationSettings
    ): Double {
        val attackSamples = (SAMPLE_RATE * durationSettings.attackSeconds).toInt().coerceAtLeast(1)
        val releaseSamples = (SAMPLE_RATE * durationSettings.releaseSeconds).toInt().coerceAtLeast(1)

        val attack = (index.toDouble() / attackSamples).coerceIn(0.0, 1.0)
        val release = ((sampleCount - index).toDouble() / releaseSamples).coerceIn(0.0, 1.0)

        return minOf(attack, release)
    }

    private fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69) / 12.0)
    }
}
