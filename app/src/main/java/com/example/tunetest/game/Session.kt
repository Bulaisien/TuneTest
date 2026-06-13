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
    var hasAnsweredCurrentQuestion: Boolean = false
        private set

    fun submitAnswer(answerIndex: Int): Boolean {
        require(answerIndex in mode.choices.indices) { "Answer index is outside the mode choices" }
        check(!hasAnsweredCurrentQuestion) { "Current question has already been answered" }

        val isCorrect = answerIndex == currentQuestion.correctAnswerIndex
        answeredCount += 1
        if (isCorrect) {
            correctCount += 1
        }
        hasAnsweredCurrentQuestion = true
        return isCorrect
    }

    fun nextQuestion() {
        currentQuestion = questionGenerator.generate()
        hasAnsweredCurrentQuestion = false
    }
}
