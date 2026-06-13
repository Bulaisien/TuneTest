package com.example.tunetest.audio

class SynthAudioEngine(
    private val synthesizer: PcmSynthesizer = DefaultPcmSynthesizer(),
    private val player: PcmPlayer = AudioTrackPcmPlayer()
) : AudioEngine {

    override fun play(prompt: AudioPrompt) {
        val samples = synthesizer.render(prompt)
        player.play(samples)
    }

    override fun stop() {
        player.stop()
    }
}
