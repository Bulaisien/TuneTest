package com.example.tunetest.settings

import android.content.Context
import com.example.tunetest.musictheory.MTConsts
import com.example.tunetest.musictheory.TriadQuality

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): GameSettings {
        return GameSettings(
            duration = DurationSettings(
                singleNoteSeconds = getDouble(KEY_SINGLE_NOTE_SECONDS, 1.5),
                intervalToneSeconds = getDouble(KEY_INTERVAL_TONE_SECONDS, 1.0),
                intervalSilenceSeconds = getDouble(KEY_INTERVAL_SILENCE_SECONDS, 0.2),
                triadSeconds = getDouble(KEY_TRIAD_SECONDS, 1.5),
                attackSeconds = getDouble(KEY_ATTACK_SECONDS, 0.015),
                releaseSeconds = getDouble(KEY_RELEASE_SECONDS, 0.05)
            ),
            musicTheory = MusicTheorySettings(
                firstMidiNumber = prefs.getInt(KEY_FIRST_MIDI_NUMBER, 54),
                lastMidiNumber = prefs.getInt(KEY_LAST_MIDI_NUMBER, 77),
                enabledNoteNames = prefs.getStringSet(KEY_ENABLED_NOTES, MTConsts.NOTE_LIST.toSet())
                    ?: MTConsts.NOTE_LIST.toSet(),
                enabledIntervals = prefs.getStringSet(
                    KEY_ENABLED_INTERVALS,
                    MTConsts.INTERVAL_LIST.indices.map(Int::toString).toSet()
                )?.mapNotNull(String::toIntOrNull)?.toSet()
                    ?: MTConsts.INTERVAL_LIST.indices.toSet(),
                enabledTriadQualities = prefs.getStringSet(
                    KEY_ENABLED_TRIADS,
                    TriadQuality.entries.map { it.name }.toSet()
                )?.mapNotNull { name ->
                    TriadQuality.entries.firstOrNull { it.name == name }
                }?.toSet() ?: TriadQuality.entries.toSet()
            ).corrected()
        )
    }

    fun save(settings: GameSettings) {
        val correctedMusicTheory = settings.musicTheory.corrected()
        prefs.edit()
            .putDouble(KEY_SINGLE_NOTE_SECONDS, settings.duration.singleNoteSeconds)
            .putDouble(KEY_INTERVAL_TONE_SECONDS, settings.duration.intervalToneSeconds)
            .putDouble(KEY_INTERVAL_SILENCE_SECONDS, settings.duration.intervalSilenceSeconds)
            .putDouble(KEY_TRIAD_SECONDS, settings.duration.triadSeconds)
            .putDouble(KEY_ATTACK_SECONDS, settings.duration.attackSeconds)
            .putDouble(KEY_RELEASE_SECONDS, settings.duration.releaseSeconds)
            .putInt(KEY_FIRST_MIDI_NUMBER, correctedMusicTheory.firstMidiNumber)
            .putInt(KEY_LAST_MIDI_NUMBER, correctedMusicTheory.lastMidiNumber)
            .putStringSet(KEY_ENABLED_NOTES, correctedMusicTheory.enabledNoteNames)
            .putStringSet(KEY_ENABLED_INTERVALS, correctedMusicTheory.enabledIntervals.map(Int::toString).toSet())
            .putStringSet(KEY_ENABLED_TRIADS, correctedMusicTheory.enabledTriadQualities.map { it.name }.toSet())
            .apply()
    }

    private fun getDouble(key: String, defaultValue: Double): Double {
        return Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))
    }

    private fun android.content.SharedPreferences.Editor.putDouble(
        key: String,
        value: Double
    ): android.content.SharedPreferences.Editor {
        return putLong(key, value.toRawBits())
    }

    private companion object {
        const val PREFS_NAME = "tune_test_settings"
        const val KEY_SINGLE_NOTE_SECONDS = "single_note_seconds"
        const val KEY_INTERVAL_TONE_SECONDS = "interval_tone_seconds"
        const val KEY_INTERVAL_SILENCE_SECONDS = "interval_silence_seconds"
        const val KEY_TRIAD_SECONDS = "triad_seconds"
        const val KEY_ATTACK_SECONDS = "attack_seconds"
        const val KEY_RELEASE_SECONDS = "release_seconds"
        const val KEY_FIRST_MIDI_NUMBER = "first_midi_number"
        const val KEY_LAST_MIDI_NUMBER = "last_midi_number"
        const val KEY_ENABLED_NOTES = "enabled_notes"
        const val KEY_ENABLED_INTERVALS = "enabled_intervals"
        const val KEY_ENABLED_TRIADS = "enabled_triads"
    }
}
