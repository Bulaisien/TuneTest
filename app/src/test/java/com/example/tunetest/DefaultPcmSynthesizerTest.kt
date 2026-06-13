package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.audio.pcm.DefaultPcmSynthesizer
import com.example.tunetest.audio.DurationConfig.ATTACK_SECONDS
import com.example.tunetest.audio.DurationConfig.INTERVAL_SILENCE_SECONDS
import com.example.tunetest.audio.DurationConfig.INTERVAL_TONE_SECONDS
import com.example.tunetest.audio.DurationConfig.RELEASE_SECONDS
import com.example.tunetest.audio.DurationConfig.SINGLE_NOTE_SECONDS
import com.example.tunetest.audio.DurationConfig.TRIAD_SECONDS
import com.example.tunetest.audio.pcm.PcmAudioConfig.MAX_AMPLITUDE
import com.example.tunetest.audio.pcm.PcmAudioConfig.SAMPLE_RATE
import com.example.tunetest.musictheory.Note
import com.example.tunetest.musictheory.TriadQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class DefaultPcmSynthesizerTest {
    private val synthesizer = DefaultPcmSynthesizer()

    @Test
    fun singleNoteRendersOneSecondOfBoundedAudio() {
        val samples = synthesizer.render(
            AudioPrompt.SingleNote(Note(69, "A4"))
        )

        assertEquals(sampleCountFor(SINGLE_NOTE_SECONDS), samples.size)
        assertEquals(0, samples.first().toInt())
        assertContainsAudio(samples)
        assertWithinAmplitudeBounds(samples)
        assertContainsMidiNote(samples.steadySection(SINGLE_NOTE_SECONDS), 69)
    }

    @Test
    fun intervalRendersTwoTonesSeparatedBySilence() {
        val firstToneSize = sampleCountFor(INTERVAL_TONE_SECONDS)
        val silenceSize = sampleCountFor(INTERVAL_SILENCE_SECONDS)
        val secondToneSize = sampleCountFor(INTERVAL_TONE_SECONDS)

        val samples = synthesizer.render(
            AudioPrompt.Interval(
                root = Note(60, "C4"),
                semitones = 7
            )
        )

        assertEquals(firstToneSize + silenceSize + secondToneSize, samples.size)
        assertContainsAudio(samples.copyOfRange(0, firstToneSize))
        assertTrue(
            samples.copyOfRange(firstToneSize, firstToneSize + silenceSize)
                .all { it.toInt() == 0 }
        )
        assertContainsAudio(samples.copyOfRange(firstToneSize + silenceSize, samples.size))
        assertWithinAmplitudeBounds(samples)
        assertContainsMidiNote(samples.steadySection(INTERVAL_TONE_SECONDS), 60)
        assertContainsMidiNote(
            samples.steadySection(
                durationSeconds = INTERVAL_TONE_SECONDS,
                offsetSeconds = INTERVAL_TONE_SECONDS + INTERVAL_SILENCE_SECONDS
            ),
            67
        )
    }

    @Test
    fun triadRendersOneSecondOfBoundedAudio() {
        val samples = synthesizer.render(
            AudioPrompt.Triad(
                root = Note(60, "C4"),
                quality = TriadQuality.MAJOR
            )
        )

        assertEquals(sampleCountFor(TRIAD_SECONDS), samples.size)
        assertEquals(0, samples.first().toInt())
        assertContainsAudio(samples)
        assertWithinAmplitudeBounds(samples)
        val steadySamples = samples.steadySection(TRIAD_SECONDS)
        assertContainsMidiNote(steadySamples, 60)
        assertContainsMidiNote(steadySamples, 64)
        assertContainsMidiNote(steadySamples, 67)
    }

    private fun assertContainsAudio(samples: ShortArray) {
        assertTrue(samples.any { it.toInt() != 0 })
    }

    private fun assertWithinAmplitudeBounds(samples: ShortArray) {
        assertTrue(samples.all { abs(it.toInt()) <= MAX_AMPLITUDE })
    }

    private fun assertContainsMidiNote(samples: ShortArray, midi: Int) {
        val expectedPower = powerAtFrequency(samples, midiToFrequency(midi))
        val lowerNeighborPower = powerAtFrequency(samples, midiToFrequency(midi - 1))
        val upperNeighborPower = powerAtFrequency(samples, midiToFrequency(midi + 1))
        val strongestNeighborPower = maxOf(lowerNeighborPower, upperNeighborPower)

        assertTrue(
            "Expected MIDI $midi to be stronger than adjacent semitones",
            expectedPower > strongestNeighborPower * 4.0
        )
    }

    private fun powerAtFrequency(samples: ShortArray, frequency: Double): Double {
        var real = 0.0
        var imaginary = 0.0

        samples.forEachIndexed { index, sample ->
            val angle = 2.0 * PI * frequency * index / SAMPLE_RATE
            real += sample * cos(angle)
            imaginary += sample * sin(angle)
        }

        return real * real + imaginary * imaginary
    }

    private fun midiToFrequency(midi: Int): Double {
        return 440.0 * 2.0.pow((midi - 69) / 12.0)
    }

    private fun ShortArray.steadySection(
        durationSeconds: Double,
        offsetSeconds: Double = 0.0
    ): ShortArray {
        val steadyStartSeconds = offsetSeconds + ATTACK_SECONDS + RELEASE_SECONDS
        val steadyEndSeconds = offsetSeconds + durationSeconds - ATTACK_SECONDS - RELEASE_SECONDS
        return sliceSeconds(steadyStartSeconds, steadyEndSeconds)
    }

    private fun ShortArray.sliceSeconds(startSeconds: Double, endSeconds: Double): ShortArray {
        return copyOfRange(
            sampleCountFor(startSeconds),
            sampleCountFor(endSeconds)
        )
    }

    private fun sampleCountFor(seconds: Double): Int {
        return (SAMPLE_RATE * seconds).toInt()
    }
}
