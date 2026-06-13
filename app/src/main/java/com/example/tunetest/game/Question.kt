package com.example.tunetest.game

import com.example.tunetest.audio.AudioPrompt

data class Question(
    val audioPrompt: AudioPrompt,
    val correctAnswerIndex: Int,
)
