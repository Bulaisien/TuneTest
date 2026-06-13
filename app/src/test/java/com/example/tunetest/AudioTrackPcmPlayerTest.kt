package com.example.tunetest

import com.example.tunetest.audio.pcm.AudioTrackPcmPlayer
import com.example.tunetest.audio.pcm.PcmTrack
import com.example.tunetest.audio.pcm.PcmTrackFactory
import com.example.tunetest.audio.PlaybackRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AudioTrackPcmPlayerTest {
    @Test
    fun playCreatesTrackWritesSamplesAndStartsPlayback() {
        val samples = shortArrayOf(1, 2, 3)
        val calls = mutableListOf<String>()
        val track = FakePcmTrack(calls)
        val factory = FakePcmTrackFactory(track, calls = calls)
        val player = AudioTrackPcmPlayer(factory, ImmediatePlaybackRunner(calls))

        player.play(samples)

        assertSame(samples, factory.createdForSamples)
        assertSame(samples, track.writtenSamples)
        assertEquals(0, track.writeOffset)
        assertEquals(samples.size, track.writeSize)
        assertEquals(listOf("run", "create", "write", "play"), calls)
    }

    @Test
    fun playStopsExistingTrackBeforeStartingNewTrack() {
        val calls = mutableListOf<String>()
        val firstTrack = FakePcmTrack(calls, name = "first")
        val secondTrack = FakePcmTrack(calls, name = "second")
        val factory = FakePcmTrackFactory(firstTrack, secondTrack, calls = calls)
        val player = AudioTrackPcmPlayer(factory, ImmediatePlaybackRunner(calls))

        player.play(shortArrayOf(1))
        player.play(shortArrayOf(2))

        assertEquals(
            listOf(
                "run",
                "create",
                "first.write",
                "first.play",
                "first.pause",
                "first.flush",
                "first.release",
                "run",
                "create",
                "second.write",
                "second.play"
            ),
            calls
        )
    }

    @Test
    fun stopPausesFlushesReleasesAndClearsCurrentTrack() {
        val calls = mutableListOf<String>()
        val firstTrack = FakePcmTrack(calls, name = "first")
        val secondTrack = FakePcmTrack(calls, name = "second")
        val factory = FakePcmTrackFactory(firstTrack, secondTrack, calls = calls)
        val player = AudioTrackPcmPlayer(factory, ImmediatePlaybackRunner(calls))

        player.play(shortArrayOf(1))
        player.stop()
        player.stop()
        player.play(shortArrayOf(2))

        assertEquals(
            listOf(
                "run",
                "create",
                "first.write",
                "first.play",
                "first.pause",
                "first.flush",
                "first.release",
                "run",
                "create",
                "second.write",
                "second.play"
            ),
            calls
        )
    }

    @Test
    fun stopStillReleasesTrackWhenPauseThrows() {
        val calls = mutableListOf<String>()
        val track = FakePcmTrack(calls, throwOnPause = true)
        val player = AudioTrackPcmPlayer(
            trackFactory = FakePcmTrackFactory(track, calls = calls),
            playbackRunner = ImmediatePlaybackRunner(calls)
        )

        player.play(shortArrayOf(1))
        player.stop()

        assertEquals(listOf("run", "create", "write", "play", "pause", "release"), calls)
    }

    @Test
    fun stopStillReleasesTrackWhenFlushThrows() {
        val calls = mutableListOf<String>()
        val track = FakePcmTrack(calls, throwOnFlush = true)
        val player = AudioTrackPcmPlayer(
            trackFactory = FakePcmTrackFactory(track, calls = calls),
            playbackRunner = ImmediatePlaybackRunner(calls)
        )

        player.play(shortArrayOf(1))
        player.stop()

        assertEquals(listOf("run", "create", "write", "play", "pause", "flush", "release"), calls)
    }

    private class ImmediatePlaybackRunner(
        private val calls: MutableList<String>
    ) : PlaybackRunner {
        override fun run(block: () -> Unit) {
            calls += "run"
            block()
        }
    }

    private class FakePcmTrackFactory(
        private vararg val tracks: FakePcmTrack,
        private val calls: MutableList<String>
    ) : PcmTrackFactory {
        private var nextTrackIndex = 0
        var createdForSamples: ShortArray? = null
            private set

        override fun create(samples: ShortArray): PcmTrack {
            calls += "create"
            createdForSamples = samples
            return tracks[nextTrackIndex++]
        }
    }

    private class FakePcmTrack(
        private val calls: MutableList<String>,
        private val name: String? = null,
        private val throwOnPause: Boolean = false,
        private val throwOnFlush: Boolean = false
    ) : PcmTrack {
        var writtenSamples: ShortArray? = null
            private set
        var writeOffset: Int? = null
            private set
        var writeSize: Int? = null
            private set

        override fun write(samples: ShortArray, offset: Int, size: Int) {
            calls += callName("write")
            writtenSamples = samples
            writeOffset = offset
            writeSize = size
        }

        override fun play() {
            calls += callName("play")
        }

        override fun pause() {
            calls += callName("pause")
            if (throwOnPause) throw IllegalStateException("Pause failed")
        }

        override fun flush() {
            calls += callName("flush")
            if (throwOnFlush) throw IllegalStateException("Flush failed")
        }

        override fun release() {
            calls += callName("release")
        }

        private fun callName(method: String): String {
            return name?.let { "$it.$method" } ?: method
        }
    }
}
