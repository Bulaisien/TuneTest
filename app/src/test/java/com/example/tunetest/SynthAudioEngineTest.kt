package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.audio.pcm.PcmPlayer
import com.example.tunetest.audio.pcm.PcmSynthesizer
import com.example.tunetest.audio.SynthAudioEngine
import com.example.tunetest.musictheory.Note
import com.example.tunetest.settings.DurationSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SynthAudioEngineTest {
    @Test
    fun playRendersPromptAndPlaysRenderedSamples() {
        val prompt = AudioPrompt.SingleNote(Note(60, "C4"))
        val renderedSamples = shortArrayOf(1, -2, 3)
        val calls = mutableListOf<String>()
        val synthesizer = FakePcmSynthesizer(renderedSamples, calls)
        val player = FakePcmPlayer(calls)
        val engine = SynthAudioEngine(synthesizer, player)
        val durationSettings = DurationSettings(singleNoteSeconds = 2.0)

        engine.play(prompt, durationSettings)

        assertEquals(prompt, synthesizer.renderedPrompt)
        assertEquals(durationSettings, synthesizer.renderedDurationSettings)
        assertSame(renderedSamples, player.playedSamples)
        assertEquals(listOf("render", "play"), calls)
    }

    @Test
    fun stopStopsPlayer() {
        val player = FakePcmPlayer()
        val engine = SynthAudioEngine(
            synthesizer = FakePcmSynthesizer(shortArrayOf()),
            player = player
        )

        engine.stop()

        assertEquals(1, player.stopCount)
    }

    private class FakePcmSynthesizer(
        private val samples: ShortArray,
        private val calls: MutableList<String> = mutableListOf()
    ) : PcmSynthesizer {
        var renderedPrompt: AudioPrompt? = null
            private set
        var renderedDurationSettings: DurationSettings? = null
            private set

        override fun render(
            prompt: AudioPrompt,
            durationSettings: DurationSettings
        ): ShortArray {
            calls += "render"
            renderedPrompt = prompt
            renderedDurationSettings = durationSettings
            return samples
        }
    }

    private class FakePcmPlayer(
        private val calls: MutableList<String> = mutableListOf()
    ) : PcmPlayer {
        var playedSamples: ShortArray? = null
            private set
        var stopCount = 0
            private set

        override fun play(samples: ShortArray) {
            calls += "play"
            playedSamples = samples
        }

        override fun stop() {
            stopCount += 1
        }
    }
}
