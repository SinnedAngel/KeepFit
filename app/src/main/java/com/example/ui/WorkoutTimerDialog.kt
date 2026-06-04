package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.HealthRoutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimerDialog(
    routine: HealthRoutine,
    languageCode: String,
    onDismiss: () -> Unit,
    onComplete: (durationMinutes: Int, caloriesBurned: Int) -> Unit
) {
    val isBreathing = routine.id.contains("breath") || 
            routine.id.contains("oxygen") || 
            routine.id.contains("healing") || 
            routine.id.contains("compaction") ||
            routine.name.lowercase().contains("breath")

    // Configuration States
    var inhaleSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 4 else 5) }
    var holdSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 2 else if (routine.id == "cell_oxygenation") 10 else 3) }
    var exhaleSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 6 else 5) }
    var restSec by remember { mutableIntStateOf(if (isBreathing) 2 else 5) }
    
    // For posture/stances
    var holdPostureSec by remember { mutableIntStateOf(if (routine.id == "sikap_basic") 30 else 45) }
    
    var totalLoops by remember { mutableIntStateOf(if (isBreathing) 5 else 3) }

    // Sound customization states
    var enableVoice by remember { mutableStateOf(true) }
    var enableAmbientSound by remember { mutableStateOf(true) }
    var isVoiceSpeaking by remember { mutableStateOf(false) }
    var voiceStartMillis by remember { mutableLongStateOf(0L) }

    // Dialog state: "CONFIG", "RUNNING", "PAUSED", "COMPLETED"
    var dialogState by remember { mutableStateOf("CONFIG") }
    
    // Active Timer States
    var currentLoop by remember { mutableIntStateOf(1) }
    var currentPhase by remember { mutableStateOf(if (isBreathing) "INHALE" else "HOLD_POSTURE") }
    var phaseSecondsRemaining by remember { mutableIntStateOf(0) }
    var totalSecondsElapsed by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    fun playTickSound() {
        val sampleRate = 22050
        val durationSamples = (sampleRate * 0.015f).toInt() // 15ms
        val buffer = ShortArray(durationSamples)
        for (i in 0 until durationSamples) {
            val t = i.toFloat() / sampleRate
            // 1500 Hz clean sine wave with an extremely sharp exponential decay for a crisp woody tick
            val amp = kotlin.math.exp(-t * 280f) * 0.12f
            val sine = kotlin.math.sin(2f * Math.PI.toFloat() * 1500f * t)
            buffer[i] = (sine * amp * 32767f).toInt().toShort()
        }
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buffer, 0, buffer.size)
            track.play()
            // Asynchronously stop and release track
            GlobalScope.launch(Dispatchers.IO) {
                delay(120)
                try {
                    track.stop()
                    track.release()
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("KeepFitAudio", "Tick sonification failed: ${e.message}")
        }
    }

    // Setup TTS Engine and Dispose on Exit
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                try {
                    val locale = if (languageCode == "ID") Locale("id", "ID") else Locale.ENGLISH
                    val result = instance?.setLanguage(locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("KeepFitTTS", "Selected locale not supported, backing up to English")
                        instance?.language = Locale.ENGLISH
                    }
                } catch (e: Exception) {
                    Log.e("KeepFitTTS", "Failed to set TTS Locale: ${e.message}")
                }
                instance?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isVoiceSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isVoiceSpeaking = false
                    }
                    @Deprecated("Deprecated in Java", ReplaceWith("isVoiceSpeaking = false"))
                    override fun onError(utteranceId: String?) {
                        isVoiceSpeaking = false
                    }
                })
            }
        }
        tts = instance
        onDispose {
            instance?.stop()
            instance?.shutdown()
        }
    }

    // Speech delivery function
    fun speakText(text: String) {
        if (enableVoice && isTtsReady) {
            try {
                isVoiceSpeaking = true
                voiceStartMillis = System.currentTimeMillis()
                val utteranceId = java.util.UUID.randomUUID().toString()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } catch (e: Exception) {
                Log.e("KeepFitTTS", "Speech delivery error: ${e.message}")
                isVoiceSpeaking = false
            }
        } else {
            isVoiceSpeaking = false
        }
    }

    // Voice announcement triggers on phase transitions
    LaunchedEffect(currentPhase, currentLoop, isTimerRunning) {
        if (isTimerRunning) {
            val announcement = when (currentPhase) {
                "INHALE" -> {
                    if (languageCode == "ID") {
                        if (currentLoop > 1) "Putaran $currentLoop. Tarik napas." else "Tarik napas."
                    } else {
                        if (currentLoop > 1) "Cycle $currentLoop. Inhale." else "Inhale."
                    }
                }
                "HOLD" -> {
                    if (languageCode == "ID") "Tahan." else "Hold."
                }
                "EXHALE" -> {
                    if (languageCode == "ID") "Hembuskan." else "Exhale."
                }
                "REST" -> {
                    if (languageCode == "ID") "Istirahat." else "Rest."
                }
                "HOLD_POSTURE" -> {
                    if (languageCode == "ID") {
                        if (currentLoop > 1) "Putaran $currentLoop. Tahan." else "Tahan."
                    } else {
                        if (currentLoop > 1) "Cycle $currentLoop. Hold." else "Hold."
                    }
                }
                else -> ""
            }
            if (announcement.isNotEmpty()) {
                speakText(announcement)
            }
        }
    }

    // Voice announcement trigger when session finishes or when starting
    LaunchedEffect(dialogState) {
        if (dialogState == "COMPLETED") {
            val endMsg = if (languageCode == "ID") "Latihan selesai. Kerja bagus!" else "Practice complete. Outstanding work!"
            speakText(endMsg)
        }
    }

    // Real-time Breathing audio flow synthesis
    LaunchedEffect(isTimerRunning, currentPhase, enableAmbientSound, isBreathing) {
        if (!isBreathing || !isTimerRunning || !enableAmbientSound || (currentPhase != "INHALE" && currentPhase != "EXHALE")) {
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            var track: AudioTrack? = null
            try {
                val sampleRate = 22050
                val maxPhaseSeconds = if (currentPhase == "INHALE") inhaleSec else exhaleSec
                val totalSamples = sampleRate * maxPhaseSeconds

                val buffer = ShortArray(totalSamples)
                val random = java.util.Random()
                
                val isInhale = currentPhase == "INHALE"
                var filterState = 0f

                // Pre-generate the complete ambient wave for this phase
                for (i in 0 until totalSamples) {
                    val progressRatio = i.toFloat() / totalSamples
                    
                    // Perfect wave envelope swell:
                    // Inhale starts at 0 and swells to maximum near the end
                    // Exhale starts at maximum and decays gracefully to 0
                    val envelope = if (isInhale) {
                        if (progressRatio < 0.85f) {
                            kotlin.math.sin(progressRatio / 0.85f * (Math.PI / 2.0)).toFloat()
                        } else {
                            kotlin.math.cos((progressRatio - 0.85f) / 0.15f * (Math.PI / 4.0)).toFloat()
                        }
                    } else {
                        kotlin.math.cos(progressRatio * (Math.PI / 2.0)).toFloat()
                    }

                    // Apply vocal speech override context
                    val finalEnvelope = if (enableVoice && isVoiceSpeaking) {
                        0f
                    } else {
                        envelope
                    }

                    // Distinct auditory frequencies (Inhale vs Exhale)
                    // Inhale: gently rising breeze/wind tone (125Hz to 140Hz)
                    // Exhale: deeper, falling grounding tone (90Hz down to 78Hz)
                    val (fBase, fHarmonic) = if (isInhale) {
                        val base = 125.0 + (15.0 * progressRatio)
                        base to (base * 1.5)
                    } else {
                        val base = 90.0 - (12.0 * progressRatio)
                        base to (base * 1.5)
                    }

                    // Sliding lowpass filter alpha for wave animation:
                    // Inhale filter cutoff opens up (brighter wind)
                    // Exhale filter cutoff rolls down (softer rumble)
                    val alpha = if (isInhale) {
                        0.02f + (0.08f * progressRatio)
                    } else {
                        0.08f - (0.075f * progressRatio)
                    }

                    // White noise
                    val white = random.nextFloat() * 2f - 1f
                    
                    // Low-pass filter the noise
                    filterState = filterState + alpha * (white - filterState)
                    val windSound = filterState * (if (isInhale) 0.35f else 0.45f)

                    // Add a soothing modulated deep organ / tibetan bowl fundamental hum to wave
                    val tSeconds = i.toDouble() / sampleRate
                    val angleBase = 2.0 * Math.PI * fBase * tSeconds
                    val angleHarmonic = 2.0 * Math.PI * fHarmonic * tSeconds
                    val sineVolume = if (isInhale) 0.08f else 0.12f
                    val sineWave = (kotlin.math.sin(angleBase) * 0.7 + kotlin.math.sin(angleHarmonic) * 0.3) * sineVolume

                    val mixed = (windSound + sineWave) * finalEnvelope * 0.45f
                    buffer[i] = (mixed * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }

                // Play the whole wave natively using static track for 100% glitch-free performance!
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Keep the coroutine suspended while the track plays to maintain life-cycle
                val durationMs = maxPhaseSeconds * 1000L
                delay(durationMs)

            } catch (e: Exception) {
                Log.e("KeepFitAudio", "Pre-gen ambient wave synth error: ${e.message}")
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    // Fail-safe cleanup
                }
            }
        }
    }

    // Calculate dynamic values for completion screen
    val totalEstimatedSeconds = remember(isBreathing, inhaleSec, holdSec, exhaleSec, restSec, holdPostureSec, totalLoops) {
        if (isBreathing) {
            (inhaleSec + holdSec + exhaleSec + restSec) * totalLoops
        } else {
            (holdPostureSec + restSec) * totalLoops
        }
    }

    // Handle stopping voice/TTS if timer is paused
    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) {
            try {
                tts?.stop()
                isVoiceSpeaking = false
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Timer Effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isTimerRunning && phaseSecondsRemaining > 0) {
                val now = System.currentTimeMillis()
                if (enableVoice && isVoiceSpeaking && (now - voiceStartMillis < 10000L)) {
                    delay(100)
                    continue
                }
                delay(1000)
                val nowAfterDelay = System.currentTimeMillis()
                if (enableVoice && isVoiceSpeaking && (nowAfterDelay - voiceStartMillis < 10000L)) {
                    continue
                }
                totalSecondsElapsed++
                phaseSecondsRemaining--
                if (enableAmbientSound) {
                    playTickSound()
                }
                
                if (phaseSecondsRemaining == 0) {
                    // Transition phase
                    if (isBreathing) {
                        when (currentPhase) {
                            "INHALE" -> {
                                if (holdSec > 0) {
                                    currentPhase = "HOLD"
                                    phaseSecondsRemaining = holdSec
                                } else {
                                    currentPhase = "EXHALE"
                                    phaseSecondsRemaining = exhaleSec
                                }
                            }
                            "HOLD" -> {
                                currentPhase = "EXHALE"
                                phaseSecondsRemaining = exhaleSec
                            }
                            "EXHALE" -> {
                                if (restSec > 0) {
                                    currentPhase = "REST"
                                    phaseSecondsRemaining = restSec
                                } else {
                                    if (currentLoop < totalLoops) {
                                        currentLoop++
                                        currentPhase = "INHALE"
                                        phaseSecondsRemaining = inhaleSec
                                    } else {
                                        isTimerRunning = false
                                        dialogState = "COMPLETED"
                                    }
                                }
                            }
                            "REST" -> {
                                if (currentLoop < totalLoops) {
                                    currentLoop++
                                    currentPhase = "INHALE"
                                    phaseSecondsRemaining = inhaleSec
                                } else {
                                    isTimerRunning = false
                                    dialogState = "COMPLETED"
                                }
                            }
                        }
                    } else {
                        // Posture holds
                        when (currentPhase) {
                            "HOLD_POSTURE" -> {
                                if (restSec > 0) {
                                    currentPhase = "REST"
                                    phaseSecondsRemaining = restSec
                                } else {
                                    if (currentLoop < totalLoops) {
                                        currentLoop++
                                        currentPhase = "HOLD_POSTURE"
                                        phaseSecondsRemaining = holdPostureSec
                                    } else {
                                        isTimerRunning = false
                                        dialogState = "COMPLETED"
                                    }
                                }
                            }
                            "REST" -> {
                                if (currentLoop < totalLoops) {
                                    currentLoop++
                                    currentPhase = "HOLD_POSTURE"
                                    phaseSecondsRemaining = holdPostureSec
                                } else {
                                    isTimerRunning = false
                                    dialogState = "COMPLETED"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = {
        isTimerRunning = false
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("workout_timer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (languageCode == "ID") {
                                if (isBreathing) "Pengukur Waktu Pernapasan Dinamis Kateda" else "Pengukur Waktu Sikap Tubuh Kateda"
                            } else {
                                if (isBreathing) "Kateda Dynamic Breathwork Timer" else "Kateda Posture Hold Timer"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = {
                        isTimerRunning = false
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                when (dialogState) {
                    "CONFIG" -> {
                        Text(
                            text = if (languageCode == "ID") "Konfigurasi Sesi Latihan" else "Configure Practice Flow",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                        ) {
                            if (isBreathing) {
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tarik Napas" else "Inhale (Breathe In)",
                                    seconds = inhaleSec,
                                    onSecondsChanged = { inhaleSec = it.coerceIn(2, 12) },
                                    color = Color(0xFF64B5F6)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tahan Napas (Tekan Perut)" else "Hold (Abs Compacted)",
                                    seconds = holdSec,
                                    onSecondsChanged = { holdSec = it.coerceIn(0, 10) },
                                    color = Color(0xFFFFD54F)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Hembuskan Napas" else "Exhale (Breathe Out)",
                                    seconds = exhaleSec,
                                    onSecondsChanged = { exhaleSec = it.coerceIn(2, 12) },
                                    color = Color(0xFFFF8A65)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Fase Istirahat" else "Rest/Recovery Phase",
                                    seconds = restSec,
                                    onSecondsChanged = { restSec = it.coerceIn(0, 10) },
                                    color = Color(0xFF80CBC4)
                                )
                            } else {
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tahan Sikap / Kuda-Kuda" else "Hold Posture / Stance",
                                    seconds = holdPostureSec,
                                    onSecondsChanged = { holdPostureSec = it.coerceIn(5, 120) },
                                    color = Color(0xFFE57373)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Istirahat Antara Sikap" else "Rest/Recovery Between Holds",
                                    seconds = restSec,
                                    onSecondsChanged = { restSec = it.coerceIn(0, 30) },
                                    color = Color(0xFF80CBC4)
                                )
                            }

                            TimerSettingRow(
                                label = if (languageCode == "ID") "Jumlah Putaran Latihan" else "Cycles/Loops Sequence",
                                seconds = totalLoops,
                                onSecondsChanged = { totalLoops = it.coerceIn(1, 15) },
                                color = MaterialTheme.colorScheme.primary,
                                unitLabel = if (languageCode == "ID") "Putaran" else "Loops"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio Configuration Controls (Vocal guidance & breathing loop wind synthesis)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Voice Guidance Card Trigger
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { enableVoice = !enableVoice }
                                    .testTag("voice_guidance_toggle_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (enableVoice) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (enableVoice) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageCode == "ID") "Panduan Suara" else "Vocal Guide",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (languageCode == "ID") "Perintah suara TTS" else "TTS voice commands",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = enableVoice,
                                        onCheckedChange = { enableVoice = it },
                                        modifier = Modifier.scale(0.7f)
                                    )
                                }
                            }

                            if (isBreathing) {
                                // Dynamic AirWave sound synthesizer Trigger
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { enableAmbientSound = !enableAmbientSound }
                                        .testTag("ambient_sound_toggle_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (enableAmbientSound) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (enableAmbientSound) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (languageCode == "ID") "Gelombang Paru" else "Lung Wave",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (languageCode == "ID") "Deru angin pernapasan" else "Breathing audio wind",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = enableAmbientSound,
                                            onCheckedChange = { enableAmbientSound = it },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Duration Summary
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Clock",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageCode == "ID") "Total Durasi Sesi" else "Total Session Duration",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = formatDuration(totalEstimatedSeconds),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                dialogState = "RUNNING"
                                currentLoop = 1
                                currentPhase = if (isBreathing) "INHALE" else "HOLD_POSTURE"
                                phaseSecondsRemaining = if (isBreathing) inhaleSec else holdPostureSec
                                totalSecondsElapsed = 0
                                isTimerRunning = true
                                val startMsg = if (languageCode == "ID") "Mulai latihan. Regangkan tubuh Anda." else "Begin practice. Expand your body."
                                speakText(startMsg)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                               .testTag("begin_practice_timer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (languageCode == "ID") "MULAI SESI LATIHAN" else "BEGIN GUIDED PRACTICE",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    "RUNNING", "PAUSED" -> {
                        // Current Phase Settings for Color scheme
                        val (phaseColor, phaseDescription) = when (currentPhase) {
                            "INHALE" -> Color(0xFF64B5F6) to (if (languageCode == "ID") "Tarik napas dalam-dalam lewat hidung, kembangkan perut bagian bawah." else "Deeply inhale through nostrils, expanding lower abdomen.")
                            "HOLD" -> Color(0xFFFFD54F) to (if (languageCode == "ID") "Tahan napas. Kunci pemadatan perut inti." else "Hold breath. Secure core abdominal compaction.")
                            "EXHALE" -> Color(0xFFFF8A65) to (if (languageCode == "ID") "Hembuskan napas perlahan, tekan ketegangan ke bawah dan luar." else "Slowly exhale, pushing tension down and out.")
                            "REST" -> Color(0xFF80CBC4) to (if (languageCode == "ID") "Rilekskan semua otot. Kalibrasi kembali aliran napas alami." else "Relax all muscles. Recalibrating natural breathing wave.")
                            "HOLD_POSTURE" -> Color(0xFFE57373) to (if (languageCode == "ID") "Sikap tegak dengan keseimbangan kokoh. Diam jangan bergerak." else "Engage posture with rigid balance. Hold still.")
                            else -> MaterialTheme.colorScheme.primary to (if (languageCode == "ID") "Pertahankan konsentrasi." else "Maintain concentration.")
                        }

                        val maxPhaseSeconds = when (currentPhase) {
                            "INHALE" -> inhaleSec
                            "HOLD" -> holdSec
                            "EXHALE" -> exhaleSec
                            "REST" -> restSec
                            "HOLD_POSTURE" -> holdPostureSec
                            else -> 5
                        }

                        // Progress fraction
                        val fraction = if (maxPhaseSeconds > 0) {
                            phaseSecondsRemaining.toFloat() / maxPhaseSeconds
                        } else 1f

                        // Pulsing radius factor based on breathing phases
                        val pulseTarget = when (currentPhase) {
                            "INHALE" -> 0.8f + 0.6f * (1f - fraction)
                            "HOLD" -> 1.4f + 0.05f * kotlin.math.sin(totalSecondsElapsed * 3f)
                            "EXHALE" -> 0.8f + 0.6f * fraction
                            "REST" -> 0.82f + 0.02f * kotlin.math.sin(totalSecondsElapsed * 1.5f)
                            "HOLD_POSTURE" -> 1.0f + 0.04f * kotlin.math.sin(totalSecondsElapsed * 4f)
                            else -> 1f
                        }

                        val animatedPulseScale by animateFloatAsState(
                            targetValue = pulseTarget,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "BreathingCorePulse"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Cycle status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Loop",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CYCLE $currentLoop OF $totalLoops",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Active Practice Screen Sound & Speech Quick Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                // Live Speech guide toggle mini trigger
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { 
                                            enableVoice = !enableVoice 
                                            if (enableVoice) {
                                                val guideMsg = if (languageCode == "ID") "Panduan suara aktif." else "Speech guide active."
                                                speakText(guideMsg)
                                            }
                                        }
                                        .background(if (enableVoice) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.1f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (enableVoice) Icons.Default.PlayArrow else Icons.Default.Close,
                                        contentDescription = "Voice Guide Activator",
                                        tint = if (enableVoice) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (enableVoice) "Voice: ON" else "Voice: OFF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (enableVoice) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }

                                if (isBreathing) {
                                    // Live Breathing wind synthesizer toggle mini trigger
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { enableAmbientSound = !enableAmbientSound }
                                            .background(if (enableAmbientSound) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (enableAmbientSound) Icons.Default.Favorite else Icons.Default.Close,
                                            contentDescription = "Auditory Wave Activator",
                                            tint = if (enableAmbientSound) MaterialTheme.colorScheme.secondary else Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (enableAmbientSound) "Synth: ON" else "Synth: OFF",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (enableAmbientSound) MaterialTheme.colorScheme.secondary else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Canvas Circular Tracker & Custom Breathing ball
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(170.dp)
                                    .padding(8.dp)
                            ) {
                                // Background Arch
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = Color.Gray.copy(alpha = 0.1f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Foreground remaining duration arc
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = phaseColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * fraction,
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Simulated organic lungs/energy core pulsing ball
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .scale(animatedPulseScale)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    phaseColor,
                                                    phaseColor.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBreathing) Icons.Default.Favorite else Icons.Default.Star,
                                        contentDescription = "Action Symbol",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Phase countdown text block superimposed
                                Text(
                                    text = "$phaseSecondsRemaining",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.offset(y = 52.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Current phase name
                            Text(
                                text = when (currentPhase) {
                                    "INHALE" -> "INHALE"
                                    "HOLD" -> "HOLD BREATH"
                                    "EXHALE" -> "EXHALE"
                                    "REST" -> "REST & DETOX"
                                    "HOLD_POSTURE" -> "HOLD STANCE"
                                    else -> "PRACTICING"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = phaseColor,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive phase descriptive tutorial instruction
                            Text(
                                text = phaseDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .height(34.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Control Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Quit/Stop Button
                                OutlinedIconButton(
                                    onClick = {
                                        isTimerRunning = false
                                        dialogState = "CONFIG"
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Stop",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                // Play Pause central toggle
                                FilledIconButton(
                                    onClick = {
                                        isTimerRunning = !isTimerRunning
                                        dialogState = if (isTimerRunning) "RUNNING" else "PAUSED"
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(52.dp)
                                        .testTag("play_pause_timer_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                        contentDescription = if (isTimerRunning) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Skip Phase Button
                                OutlinedIconButton(
                                    onClick = {
                                        phaseSecondsRemaining = 1
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = "Skip Phase",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Overall Elapsed
                            Text(
                                text = "Total elapsed: ${formatDuration(totalSecondsElapsed)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    "COMPLETED" -> {
                        val finalDurationMinutes = maxOf(1, (totalSecondsElapsed / 60))
                        val calculatedCaloriesBurned = remember(finalDurationMinutes, routine) {
                            val progressRatio = totalSecondsElapsed.toFloat() / totalEstimatedSeconds.coerceAtLeast(1)
                            val k = (routine.caloriesBurned * progressRatio).toInt()
                            maxOf(10, k)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timer_complete_screen")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Finished",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Kateda Flow Completed!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Your physical vessels are oxygenated and central energy has been harmonized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Report Stats
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "TIME SPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatDuration(totalSecondsElapsed),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "ESTIMATED BURN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$calculatedCaloriesBurned kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "CYCLES DONE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$totalLoops / $totalLoops",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onComplete(finalDurationMinutes, calculatedCaloriesBurned)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_practice_history_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "SAVE PRACTICE TO DIARY",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Close without logging",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerSettingRow(
    label: String,
    seconds: Int,
    onSecondsChanged: (Int) -> Unit,
    color: Color,
    unitLabel: String = "Sec"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$seconds $unitLabel",
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Minus Button
            OutlinedIconButton(
                onClick = { onSecondsChanged(seconds - if (unitLabel == "Loops") 1 else if (seconds <= 10) 1 else 5) },
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            }

            // Slider in center
            Slider(
                value = seconds.toFloat(),
                onValueChange = { onSecondsChanged(it.toInt()) },
                valueRange = if (unitLabel == "Loops") 1f..15f else if (label.contains("Post")) 5f..120f else 0f..12f,
                modifier = Modifier.width(80.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f),
                    thumbColor = color
                )
            )

            // Plus Button
            OutlinedIconButton(
                onClick = { onSecondsChanged(seconds + if (unitLabel == "Loops") 1 else if (seconds < 10) 1 else 5) },
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            }
        }
    }
}
