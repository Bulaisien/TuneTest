package com.example.tunetest.game

class Session(
    val mode: GameMode,
    private val questionGenerator: QuestionGenerator = mode.qGen
) {
    var correctCount: Int = 0
        private set
    var answeredCount: Int = 0
        private set

    var currentQuestion: Question = questionGenerator.generate()
        private set

    fun submitAnswer(answerIndex: Int): Boolean {
        require(answerIndex in mode.choices.indices) { "Answer index is outside the mode choices" }

        val isCorrect = answerIndex == currentQuestion.correctAnswerIndex
        answeredCount += 1
        if (isCorrect) {
            correctCount += 1
        }
        currentQuestion = questionGenerator.generate()
        return isCorrect
    }
}
