package com.example.tunetest.audio

import com.example.tunetest.audio.pcm.AudioTrackPcmPlayer
import com.example.tunetest.audio.pcm.DefaultPcmSynthesizer
import com.example.tunetest.audio.pcm.PcmPlayer
import com.example.tunetest.audio.pcm.PcmSynthesizer
import com.example.tunetest.settings.DurationSettings

class SynthAudioEngine(
    private val synthesizer: PcmSynthesizer = DefaultPcmSynthesizer(),
    private val player: PcmPlayer = AudioTrackPcmPlayer()
) : AudioEngine {

    override fun play(prompt: AudioPrompt, durationSettings: DurationSettings) {
        val samples = synthesizer.render(prompt, durationSettings)
        player.play(samples)
    }

    override fun stop() {
        player.stop()
    }
}
