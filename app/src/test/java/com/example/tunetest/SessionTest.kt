package com.example.tunetest

import com.example.tunetest.audio.AudioPrompt
import com.example.tunetest.game.GameMode
import com.example.tunetest.game.Question
import com.example.tunetest.game.QuestionGenerator
import com.example.tunetest.game.Session
import com.example.tunetest.musictheory.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTest {
    @Test
    fun startsWithGeneratedQuestionAndZeroScore() {
        val firstQuestion = question(correctAnswerIndex = 0)
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(firstQuestion)
        )

        assertSame(firstQuestion, session.currentQuestion)
        assertEquals(0, session.answeredCount)
        assertEquals(0, session.correctCount)
        assertFalse(session.hasAnsweredCurrentQuestion)
    }

    @Test
    fun correctAnswerIncrementsAnsweredAndCorrectCountsWithoutAdvancingQuestion() {
        val firstQuestion = question(correctAnswerIndex = 2)
        val secondQuestion = question(correctAnswerIndex = 5)
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(firstQuestion, secondQuestion)
        )

        val wasCorrect = session.submitAnswer(2)

        assertTrue(wasCorrect)
        assertEquals(1, session.answeredCount)
        assertEquals(1, session.correctCount)
        assertTrue(session.hasAnsweredCurrentQuestion)
        assertSame(firstQuestion, session.currentQuestion)
    }

    @Test
    fun incorrectAnswerIncrementsOnlyAnsweredCountWithoutAdvancingQuestion() {
        val firstQuestion = question(correctAnswerIndex = 2)
        val secondQuestion = question(correctAnswerIndex = 5)
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(firstQuestion, secondQuestion)
        )

        val wasCorrect = session.submitAnswer(1)

        assertFalse(wasCorrect)
        assertEquals(1, session.answeredCount)
        assertEquals(0, session.correctCount)
        assertTrue(session.hasAnsweredCurrentQuestion)
        assertSame(firstQuestion, session.currentQuestion)
    }

    @Test
    fun nextQuestionAdvancesAfterAnswer() {
        val firstQuestion = question(correctAnswerIndex = 2)
        val secondQuestion = question(correctAnswerIndex = 5)
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(firstQuestion, secondQuestion)
        )

        session.submitAnswer(2)
        session.nextQuestion()

        assertSame(secondQuestion, session.currentQuestion)
        assertFalse(session.hasAnsweredCurrentQuestion)
        assertEquals(1, session.answeredCount)
        assertEquals(1, session.correctCount)
    }

    @Test
    fun rejectsSubmittingMoreThanOneAnswerForCurrentQuestion() {
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(question(correctAnswerIndex = 0))
        )

        session.submitAnswer(0)

        assertThrows(IllegalStateException::class.java) {
            session.submitAnswer(0)
        }
    }

    @Test
    fun rejectsAnswerIndexOutsideModeChoices() {
        val session = Session(
            mode = GameMode.SINGLE_NOTE,
            questionGenerator = FakeQuestionGenerator(question(correctAnswerIndex = 0))
        )

        assertThrows(IllegalArgumentException::class.java) {
            session.submitAnswer(GameMode.SINGLE_NOTE.choices.size)
        }
    }

    private fun question(correctAnswerIndex: Int): Question {
        return Question(
            audioPrompt = AudioPrompt.SingleNote(Note(60, "C4")),
            correctAnswerIndex = correctAnswerIndex
        )
    }

    private class FakeQuestionGenerator(
        private vararg val questions: Question
    ) : QuestionGenerator {
        private var index = 0

        override fun generate(): Question {
            return questions[index++]
        }
    }
}
