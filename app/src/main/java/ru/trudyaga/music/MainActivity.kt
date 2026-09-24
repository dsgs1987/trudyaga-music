package ru.trudyaga.music

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

data class Track(
    val title: String,
    val subtitle: String,
    val c1: Color,
    val c2: Color,
    val icon: String
)

private val tracks = listOf(
    Track("Натяжка СИП", "Трудяга · 03:42", Color(0xFF0D47A1), Color(0xFF42A5F5), "⚡"),
    Track("Опора №37", "Северные линии · 04:18", Color(0xFF263238), Color(0xFF90A4AE), "37"),
    Track("Монтаж ВЛ", "Трудяга · 03:56", Color(0xFF6A1B9A), Color(0xFFCE93D8), "⌁"),
    Track("Тяжёлый день", "Трудяга · 05:01", Color(0xFFBF360C), Color(0xFFFFB74D), "☀"),
    Track("Северные линии", "Трудяга · 04:27", Color(0xFF00695C), Color(0xFF80CBC4), "✦"),
    Track("Инструмент", "Трудяга · 02:58", Color(0xFF37474F), Color(0xFFB0BEC5), "🔧")
)

private object DemoPlayer {
    private const val SAMPLE_RATE = 44100
    private const val CHANNEL = AudioFormat.CHANNEL_OUT_MONO
    private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private val executor = Executors.newSingleThreadExecutor()
    private var audioTrack: AudioTrack? = null
    private var currentIndex = -1

    fun play(index: Int) {
        executor.execute {
            if (currentIndex != index) {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
                currentIndex = index
            }
            if (audioTrack == null) {
                val samples = makeMusic(index, 20)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(ENCODING)
                            .setChannelMask(CHANNEL)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                audioTrack = track
            }
            audioTrack?.play()
        }
    }

    fun pause() {
        executor.execute { audioTrack?.pause() }
    }

    fun stop() {
        executor.execute {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
            currentIndex = -1
        }
    }

    private fun makeMusic(index: Int, seconds: Int): ShortArray {
        val total = SAMPLE_RATE * seconds
        val out = ShortArray(total)
        val melodies = arrayOf(
            intArrayOf(261, 329, 392, 523, 392, 329, 293, 349),
            intArrayOf(196, 247, 294, 392, 330, 294, 247, 220),
            intArrayOf(330, 392, 494, 659, 494, 392, 349, 440),
            intArrayOf(220, 277, 330, 440, 370, 330, 277, 247),
            intArrayOf(262, 294, 349, 440, 523, 440, 349, 294),
            intArrayOf(147, 196, 220, 294, 247, 220, 196, 165)
        )
        val notes = melodies[index % melodies.size]
        val beat = SAMPLE_RATE / 3
        for (i in 0 until total) {
            val note = notes[(i / beat) % notes.size]
            val local = i % beat
            val envelope = if (local < SAMPLE_RATE / 80) {
                local.toDouble() / (SAMPLE_RATE / 80)
            } else {
                ((beat - local).coerceAtLeast(1).toDouble() / beat).coerceAtMost(1.0)
            }
            val fundamental = sin(2.0 * PI * note * i / SAMPLE_RATE)
            val harmonic = 0.32 * sin(2.0 * PI * note * 2 * i / SAMPLE_RATE)
            out[i] = (12000.0 * (fundamental + harmonic) * envelope).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }
}

@Composable
fun Cover(t: Track, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(listOf(t.c1, t.c2)))
            drawCircle(Color.White.copy(alpha = .08f), size.minDimension * .32f, center = Offset(size.width * .78f, size.height * .22f))
            drawCircle(Color.Black.copy(alpha = .14f), size.minDimension * .5f, center = Offset(size.width * .15f, size.height * .88f))
        }
        Text(t.icon, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TrackRow(
    track: Track,
    index: Int,
    current: Track,
    favorites: Set<Int>,
    onSelect: () -> Unit,
    onFavorite: () -> Unit,
    muted: Color
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(track, Modifier.size(62.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(track.subtitle, color = muted, fontSize = 12.sp)
        }
        Text(
            if (favorites.contains(index)) "♥" else "♡",
            color = if (favorites.contains(index)) Color.White else muted,
            fontSize = 22.sp,
            modifier = Modifier.clickable(onClick = onFavorite)
        )
        Spacer(Modifier.width(12.dp))
        if (current == track) {
            Text("▶", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun App() {
    var tab by remember { mutableIntStateOf(0) }
    var category by remember { mutableIntStateOf(0) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }
    var query by remember { mutableStateOf("") }

    val current = tracks[currentIndex]
    val bg = Color(0xFF090A0F)
    val card = Color(0xFF151821)
    val muted = Color(0xFF969BA8)

    fun selectTrack(index: Int) {
        currentIndex = index
        playing = true
        DemoPlayer.play(index)
    }

    fun togglePlay() {
        playing = !playing
        if (playing) DemoPlayer.play(currentIndex) else DemoPlayer.pause()
    }

    val visibleTracks = when (tab) {
        3 -> tracks.mapIndexedNotNull { i, t -> if (favorites.contains(i)) i to t else null }
        1 -> tracks.mapIndexedNotNull { i, t -> if (t.title.contains(query, true) || t.subtitle.contains(query, true)) i to t else null }
        else -> {
            val filtered = when (category) {
                1 -> tracks.drop(2)
                2 -> tracks.take(2)
                3 -> tracks.mapIndexedNotNull { i, t -> if (favorites.contains(i)) t else null }
                else -> tracks
            }
            filtered.map { t -> tracks.indexOf(t) to t }
        }
    }

    DisposableEffect(Unit) {
        onDispose { DemoPlayer.stop() }
    }

    Surface(color = bg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(top = 14.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Трудяга Music", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("Музыка для рабочего дня", color = muted, fontSize = 13.sp)
                }
                Text("•••", color = Color.White, fontSize = 25.sp)
            }

            if (tab == 0) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    listOf("Для тебя", "Энергия", "Спокойно", "Избранное").forEachIndexed { i, s ->
                        AssistChip(
                            onClick = { category = i },
                            label = { Text(s) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (category == i) Color.White else card,
                                labelColor = if (category == i) Color.Black else Color.White
                            )
                        )
                    }
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                when (tab) {
                    0 -> {
                        Text("Твои треки", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            tracks.take(2).forEachIndexed { i, t ->
                                Column(Modifier.width(165.dp).clickable { selectTrack(i) }) {
                                    Cover(t, Modifier.size(165.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(t.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(t.subtitle, color = muted, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Text("Плейлисты", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                    }
                    1 -> {
                        Text("Поиск", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Название трека или исполнитель") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = muted
                            )
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                    2 -> {
                        Text("Моя музыка", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                    }
                    3 -> {
                        Text("Избранное", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                visibleTracks.forEach { (index, track) ->
                    TrackRow(
                        track = track,
                        index = index,
                        current = current,
                        favorites = favorites,
                        onSelect = { selectTrack(index) },
                        onFavorite = {
                            favorites = if (favorites.contains(index)) favorites - index else favorites + index
                        },
                        muted = muted
                    )
                }

                if (visibleTracks.isEmpty()) {
                    Text("Здесь пока пусто", color = muted, modifier = Modifier.padding(vertical = 30.dp))
                }
                Spacer(Modifier.height(90.dp))
            }

            Surface(
                color = card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Cover(current, Modifier.size(54.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(current.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(current.subtitle, color = muted, fontSize = 11.sp)
                    }
                    Text(
                        if (favorites.contains(currentIndex)) "♥" else "♡",
                        color = Color.White,
                        fontSize = 25.sp,
                        modifier = Modifier.clickable {
                            favorites = if (favorites.contains(currentIndex)) favorites - currentIndex else favorites + currentIndex
                        }
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        if (playing) "Ⅱ" else "▶",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { togglePlay() }
                    )
                    Spacer(Modifier.width(5.dp))
                }
            }

            Row(
                Modifier.fillMaxWidth().background(bg).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("⌂" to "Главная", "⌕" to "Поиск", "♫" to "Моя музыка", "♡" to "Избранное")
                    .forEachIndexed { i, p ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { tab = i }
                        ) {
                            Text(p.first, color = if (tab == i) Color.White else muted, fontSize = 22.sp)
                            Text(p.second, color = if (tab == i) Color.White else muted, fontSize = 10.sp)
                        }
                    }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { App() }
    }
}
