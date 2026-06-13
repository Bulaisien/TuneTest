package com.example.tunetest.game

import com.example.tunetest.musictheory.MTConsts

enum class GameMode(
    val qGen: QuestionGenerator,
    val choices: List<String>
) {
    SINGLE_NOTE(
        SingleNoteGenerator,
        MTConsts.NOTE_LIST
    ),
    INTERVAL(
        IntervalGenerator,
        MTConsts.INTERVAL_LIST
    ),
    TRIAD(
        TriadGenerator,
        MTConsts.TRIAD_QUALITY_LIST
    ),
}
