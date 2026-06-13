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
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.tunetest.musictheory.MTConsts
import com.example.tunetest.musictheory.TriadQuality
import com.example.tunetest.settings.DurationSettings
import com.example.tunetest.settings.GameSettings
import com.example.tunetest.settings.MusicTheorySettings
import com.example.tunetest.settings.SettingsStore
import com.example.tunetest.settings.displayName
import com.example.tunetest.ui.theme.TuneTestTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsStore = remember { SettingsStore(applicationContext) }
            var settings by remember { mutableStateOf(settingsStore.load()) }

            TuneTestTheme {
                TuneTestApp(
                    audioEngine = remember { SynthAudioEngine() },
                    settings = settings,
                    onSettingsChange = { newSettings ->
                        val correctedSettings = newSettings.copy(
                            musicTheory = newSettings.musicTheory.corrected()
                        )
                        settings = correctedSettings
                        settingsStore.save(correctedSettings)
                    }
                )
            }
        }
    }
}

@Composable
fun TuneTestApp(
    audioEngine: AudioEngine,
    settings: GameSettings = GameSettings(),
    onSettingsChange: (GameSettings) -> Unit = {}
) {
    DisposableEffect(audioEngine) {
        onDispose { audioEngine.stop() }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        GameScreen(
            settings = settings,
            modifier = Modifier.padding(innerPadding),
            onPlayPrompt = { session ->
                audioEngine.play(session.currentQuestion.audioPrompt, settings.duration)
            },
            onSettingsChange = onSettingsChange
        )
    }
}

@Composable
fun GameScreen(
    settings: GameSettings = GameSettings(),
    modifier: Modifier = Modifier,
    onPlayPrompt: (Session) -> Unit,
    onSettingsChange: (GameSettings) -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var showingSettings by remember { mutableStateOf(false) }

    if (showingSettings) {
        SettingsScreen(
            settings = settings,
            modifier = modifier,
            onSettingsChange = onSettingsChange,
            onBack = { showingSettings = false }
        )
    } else if (selectedMode == null) {
        HomeScreen(
            modifier = modifier,
            onModeSelected = { mode -> selectedMode = mode },
            onSettingsSelected = { showingSettings = true }
        )
    } else {
        PlayScreen(
            mode = selectedMode!!,
            settings = settings,
            modifier = modifier,
            onBack = { selectedMode = null },
            onPlayPrompt = onPlayPrompt
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onModeSelected: (GameMode) -> Unit,
    onSettingsSelected: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        TuneTestTitle()
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

        OutlinedButton(
            onClick = onSettingsSelected,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Settings")
        }
    }
}

@Composable
private fun PlayScreen(
    mode: GameMode,
    settings: GameSettings,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlayPrompt: (Session) -> Unit
) {
    var answeredCount by remember(mode) { mutableIntStateOf(0) }
    var correctCount by remember(mode) { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember(mode) { mutableStateOf<Int?>(null) }
    var hasAnsweredCurrentQuestion by remember(mode) { mutableStateOf(false) }
    var questionVersion by remember(mode) { mutableIntStateOf(0) }
    val session = remember(mode, settings.musicTheory) {
        Session(mode, settings.musicTheory)
    }

    LaunchedEffect(mode, questionVersion) {
        onPlayPrompt(session)
    }

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
            Text("Play audio")
        }

        AnswerChoices(
            choices = mode.choices(settings.musicTheory),
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
                questionVersion += 1
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
private fun SettingsScreen(
    settings: GameSettings,
    modifier: Modifier = Modifier,
    onSettingsChange: (GameSettings) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TuneTestTitle()
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium
        )

        DurationSettingsControls(
            settings = settings.duration,
            onSettingsChange = { duration ->
                onSettingsChange(settings.copy(duration = duration))
            }
        )

        MusicTheorySettingsControls(
            settings = settings.musicTheory,
            onSettingsChange = { musicTheory ->
                onSettingsChange(settings.copy(musicTheory = musicTheory.corrected()))
            }
        )

        Button(
            onClick = { onSettingsChange(GameSettings()) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Reset defaults")
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
private fun DurationSettingsControls(
    settings: DurationSettings,
    onSettingsChange: (DurationSettings) -> Unit
) {
    SettingsGroup(title = "Durations") {
        DoubleSliderSetting(
            label = "Single note",
            value = settings.singleNoteSeconds,
            range = 0.2f..4.0f,
            steps = 37,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(singleNoteSeconds = it)) }
        )
        DoubleSliderSetting(
            label = "Interval tone",
            value = settings.intervalToneSeconds,
            range = 0.2f..4.0f,
            steps = 37,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(intervalToneSeconds = it)) }
        )
        DoubleSliderSetting(
            label = "Interval silence",
            value = settings.intervalSilenceSeconds,
            range = 0.0f..2.0f,
            steps = 19,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(intervalSilenceSeconds = it)) }
        )
        DoubleSliderSetting(
            label = "Triad",
            value = settings.triadSeconds,
            range = 0.2f..4.0f,
            steps = 37,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(triadSeconds = it)) }
        )
        DoubleSliderSetting(
            label = "Attack",
            value = settings.attackSeconds,
            range = 0.005f..0.2f,
            steps = 38,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(attackSeconds = it)) }
        )
        DoubleSliderSetting(
            label = "Release",
            value = settings.releaseSeconds,
            range = 0.005f..0.5f,
            steps = 48,
            suffix = "s",
            onValueChange = { onSettingsChange(settings.copy(releaseSeconds = it)) }
        )
    }
}

@Composable
private fun MusicTheorySettingsControls(
    settings: MusicTheorySettings,
    onSettingsChange: (MusicTheorySettings) -> Unit
) {
    SettingsGroup(title = "Music theory") {
        IntSliderSetting(
            label = "Lowest MIDI note",
            value = settings.firstMidiNumber,
            range = MusicTheorySettings.MIN_MIDI_NUMBER.toFloat()..settings.lastMidiNumber.toFloat(),
            onValueChange = { onSettingsChange(settings.copy(firstMidiNumber = it)) }
        )
        IntSliderSetting(
            label = "Highest MIDI note",
            value = settings.lastMidiNumber,
            range = settings.firstMidiNumber.toFloat()..MusicTheorySettings.MAX_MIDI_NUMBER.toFloat(),
            onValueChange = { onSettingsChange(settings.copy(lastMidiNumber = it)) }
        )

        Text("Notes", style = MaterialTheme.typography.titleSmall)
        MTConsts.NOTE_LIST.forEach { noteName ->
            CheckboxSetting(
                label = noteName,
                checked = noteName in settings.enabledNoteNames,
                onCheckedChange = { checked ->
                    val notes = settings.enabledNoteNames.toggled(noteName, checked)
                    onSettingsChange(settings.copy(enabledNoteNames = notes))
                }
            )
        }

        Text("Intervals", style = MaterialTheme.typography.titleSmall)
        MTConsts.INTERVAL_LIST.forEachIndexed { index, interval ->
            CheckboxSetting(
                label = interval,
                checked = index in settings.enabledIntervals,
                onCheckedChange = { checked ->
                    val intervals = settings.enabledIntervals.toggled(index, checked)
                    onSettingsChange(settings.copy(enabledIntervals = intervals))
                }
            )
        }

        Text("Triads", style = MaterialTheme.typography.titleSmall)
        TriadQuality.entries.forEach { quality ->
            CheckboxSetting(
                label = quality.displayName,
                checked = quality in settings.enabledTriadQualities,
                onCheckedChange = { checked ->
                    val qualities = settings.enabledTriadQualities.toggled(quality, checked)
                    onSettingsChange(settings.copy(enabledTriadQualities = qualities))
                }
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun DoubleSliderSetting(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    suffix: String,
    onValueChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label: ${"%.3f".format(value)}$suffix")
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
private fun IntSliderSetting(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start).roundToInt().coerceAtLeast(0)
        )
    }
}

@Composable
private fun CheckboxSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(label)
    }
}

@Composable
private fun TuneTestTitle() {
    Text(
        text = "Tune Test",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Header(
    mode: GameMode,
    correctCount: Int,
    answeredCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TuneTestTitle()
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

private fun <T> Set<T>.toggled(value: T, enabled: Boolean): Set<T> {
    return if (enabled) {
        this + value
    } else {
        this - value
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    TuneTestTheme {
        GameScreen(onPlayPrompt = {})
    }
}
