package com.example.tunetest.game

data class Session(
    val mode: GameMode,
    val correctCount: Int = 0,
    val answeredCount: Int = 0,
    val currentQuestion: Question,
)
