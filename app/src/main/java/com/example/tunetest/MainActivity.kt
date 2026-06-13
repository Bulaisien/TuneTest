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
    var lastAnswerWasCorrect by remember(mode) { mutableStateOf<Boolean?>(null) }
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
            answeredCount = answeredCount,
            lastAnswerWasCorrect = lastAnswerWasCorrect
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
            onAnswerSelected = { answerIndex ->
                lastAnswerWasCorrect = session.submitAnswer(answerIndex)
                answeredCount = session.answeredCount
                correctCount = session.correctCount
            }
        )

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
    answeredCount: Int,
    lastAnswerWasCorrect: Boolean?
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
        lastAnswerWasCorrect?.let { wasCorrect ->
            Text(
                text = if (wasCorrect) "Correct" else "Try the next one",
                color = if (wasCorrect) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AnswerChoices(
    choices: List<String>,
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
                        ElevatedButton(
                            onClick = { onAnswerSelected(index) },
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
