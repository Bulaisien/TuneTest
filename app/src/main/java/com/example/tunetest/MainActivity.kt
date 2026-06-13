package com.example.tunetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tunetest.audio.AudioEngine
import com.example.tunetest.audio.SynthAudioEngine
import com.example.tunetest.game.GameMode
import com.example.tunetest.game.Session
import com.example.tunetest.ui.theme.TuneTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuneTestTheme {
                TuneTestApp(audioEngine = remember { SynthAudioEngine() })
            }
        }
    }
}

@Composable
fun TuneTestApp(audioEngine: AudioEngine) {
    DisposableEffect(audioEngine) {
        onDispose { audioEngine.stop() }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        GameScreen(
            modifier = Modifier.padding(innerPadding),
            onPlayPrompt = { session ->
                audioEngine.play(session.currentQuestion.audioPrompt)
            }
        )
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onPlayPrompt: (Session) -> Unit
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }

    if (selectedMode == null) {
        HomeScreen(
            modifier = modifier,
            onModeSelected = { mode -> selectedMode = mode }
        )
    } else {
        PlayScreen(
            mode = selectedMode!!,
            modifier = modifier,
            onBack = { selectedMode = null },
            onPlayPrompt = onPlayPrompt
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onModeSelected: (GameMode) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "TuneTest",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose a challenge",
            style = MaterialTheme.typography.titleMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GameMode.entries.forEach { mode ->
                Button(
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(mode.label)
                }
            }
        }
    }
}

@Composable
private fun PlayScreen(
    mode: GameMode,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlayPrompt: (Session) -> Unit
) {
    var answeredCount by remember(mode) { mutableIntStateOf(0) }
    var correctCount by remember(mode) { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember(mode) { mutableStateOf<Int?>(null) }
    var hasAnsweredCurrentQuestion by remember(mode) { mutableStateOf(false) }
    val session = remember(mode) { Session(mode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Header(
            mode = mode,
            correctCount = correctCount,
            answeredCount = answeredCount
        )

        Button(
            onClick = { onPlayPrompt(session) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Play question")
        }

        AnswerChoices(
            choices = mode.choices,
            enabled = !hasAnsweredCurrentQuestion,
            selectedAnswerIndex = selectedAnswerIndex,
            correctAnswerIndex = session.currentQuestion.correctAnswerIndex,
            onAnswerSelected = { answerIndex ->
                session.submitAnswer(answerIndex)
                selectedAnswerIndex = answerIndex
                answeredCount = session.answeredCount
                correctCount = session.correctCount
                hasAnsweredCurrentQuestion = session.hasAnsweredCurrentQuestion
            }
        )

        Button(
            onClick = {
                session.nextQuestion()
                selectedAnswerIndex = null
                hasAnsweredCurrentQuestion = session.hasAnsweredCurrentQuestion
            },
            enabled = hasAnsweredCurrentQuestion,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Next question")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun Header(
    mode: GameMode,
    correctCount: Int,
    answeredCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "TuneTest",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = mode.label,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Score: $correctCount / $answeredCount",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun AnswerChoices(
    choices: List<String>,
    enabled: Boolean,
    selectedAnswerIndex: Int?,
    correctAnswerIndex: Int,
    onAnswerSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Answer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        choices.mapIndexed { index, choice -> index to choice }
            .chunked(2)
            .forEach { rowChoices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowChoices.forEach { (index, choice) ->
                        val answerColor = answerButtonColor(
                            index = index,
                            selectedAnswerIndex = selectedAnswerIndex,
                            correctAnswerIndex = correctAnswerIndex
                        )
                        ElevatedButton(
                            onClick = { onAnswerSelected(index) },
                            enabled = enabled,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = answerColor.containerColor,
                                contentColor = answerColor.contentColor,
                                disabledContainerColor = answerColor.containerColor,
                                disabledContentColor = answerColor.contentColor
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(choice)
                        }
                    }
                    if (rowChoices.size == 1) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                        )
                    }
                }
            }
    }
}

@Composable
private fun answerButtonColor(
    index: Int,
    selectedAnswerIndex: Int?,
    correctAnswerIndex: Int
): AnswerButtonColor {
    if (selectedAnswerIndex == null) {
        return AnswerButtonColor(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (index == correctAnswerIndex) {
        return AnswerButtonColor(
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White
        )
    }

    if (index == selectedAnswerIndex) {
        return AnswerButtonColor(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }

    return AnswerButtonColor(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

private data class AnswerButtonColor(
    val containerColor: Color,
    val contentColor: Color
)

private val GameMode.label: String
    get() = name.lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    TuneTestTheme {
        GameScreen(onPlayPrompt = {})
    }
}
