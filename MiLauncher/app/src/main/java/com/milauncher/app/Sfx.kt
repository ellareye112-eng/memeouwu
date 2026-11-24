package com.milauncher.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/** Nota sintética. type: 0 seno, 1 cuadrada, 2 triangular, 3 sierra, 4 ruido. f1 > 0 = deslizamiento de frecuencia. */
class Note(val f0: Float, val f1: Float, val dur: Int, val type: Int, val vol: Float, val at: Int = 0)

object Sfx {
    private const val SR = 22050
    private val main = Handler(Looper.getMainLooper())
    private val cache = HashMap<String, ShortArray>()

    private fun seq(freqs: FloatArray, type: Int, dur: Int, gap: Int, vol: Float) =
        freqs.mapIndexed { i, f -> Note(f, 0f, dur, type, vol, i * gap) }

    private val recipes: Map<String, List<Note>> = mapOf(
        "clic" to listOf(Note(1500f, 900f, 50, 1, .35f)),
        "pop" to listOf(Note(420f, 120f, 130, 0, 1f)),
        "gota" to listOf(Note(1000f, 480f, 200, 0, 1f)),
        "burbuja" to listOf(Note(280f, 760f, 160, 0, 1f)),
        "plop" to listOf(Note(500f, 200f, 100, 0, 1f), Note(620f, 250f, 100, 0, 1f, 120)),
        "campana" to listOf(Note(1318f, 0f, 700, 0, .7f), Note(1976f, 0f, 500, 0, .4f)),
        "cristal" to seq(floatArrayOf(2349f, 2637f, 3136f), 0, 500, 70, .35f),
        "marimba" to listOf(Note(523f, 0f, 350, 2, .8f), Note(1046f, 0f, 150, 0, .3f)),
        "xilofono" to seq(floatArrayOf(784f, 988f, 1175f), 2, 250, 70, .6f),
        "moneda" to listOf(Note(988f, 0f, 90, 1, .3f), Note(1319f, 0f, 350, 1, .3f, 80)),
        "destello" to seq(floatArrayOf(1047f, 1319f, 1568f, 2093f), 2, 180, 60, .6f),
        "magia" to seq(floatArrayOf(523f, 587f, 659f, 784f, 880f, 1047f, 1175f, 1319f), 2, 200, 50, .5f),
        "arpa" to seq(floatArrayOf(523f, 659f, 784f, 1047f), 0, 500, 90, .6f),
        "arcade" to seq(floatArrayOf(440f, 660f, 880f, 1320f), 1, 100, 60, .25f),
        "salto" to listOf(Note(200f, 900f, 180, 1, .3f)),
        "laser" to listOf(Note(1800f, 200f, 220, 3, .3f)),
        "alerta" to listOf(Note(880f, 0f, 120, 1, .3f), Note(880f, 0f, 120, 1, .3f, 160)),
        "tambor" to listOf(Note(160f, 50f, 250, 0, 1f), Note(0f, 0f, 80, 4, .3f)),
        "bongo" to listOf(Note(330f, 200f, 150, 0, 1f)),
        "tecla" to listOf(Note(0f, 0f, 40, 4, .6f), Note(2000f, 1500f, 30, 1, .15f)),
        "chasquido" to listOf(Note(0f, 0f, 50, 4, .9f)),
        "zen" to listOf(Note(220f, 0f, 1200, 0, .8f), Note(330f, 0f, 1200, 0, .4f)),
        "ninguno" to emptyList()
    )

    val options = listOf("clic" to "Clic", "pop" to "Pop", "gota" to "Gota", "burbuja" to "Burbuja", "plop" to "Plop doble",
        "campana" to "Campanilla", "cristal" to "Cristal", "marimba" to "Marimba", "xilofono" to "Xilófono", "moneda" to "Moneda",
        "destello" to "Destello", "magia" to "Magia", "arpa" to "Arpa", "arcade" to "Arcade", "salto" to "Salto", "laser" to "Láser",
        "alerta" to "Alerta", "tambor" to "Tambor", "bongo" to "Bongo", "tecla" to "Tecla", "chasquido" to "Chasquido",
        "zen" to "Zen", "ninguno" to "Silencio")

    private fun render(notes: List<Note>): ShortArray {
        if (notes.isEmpty()) return ShortArray(0)
        val total = notes.maxOf { it.at + it.dur } * SR / 1000 + 200
        val buf = FloatArray(total)
        for (n in notes) {
            val start = n.at * SR / 1000
            val len = n.dur * SR / 1000
            var phase = 0.0
            for (i in 0 until len) {
                if (start + i >= total) break
                val t = i.toFloat() / len
                val fr = if (n.f1 > 0f && n.f0 > 0f) n.f0 * (n.f1 / n.f0).pow(t) else n.f0
                phase += 2.0 * PI * fr / SR
                val w: Float = when (n.type) {
                    0 -> sin(phase).toFloat()
                    1 -> if (sin(phase) >= 0.0) 1f else -1f
                    2 -> (2.0 / PI * asin(sin(phase))).toFloat()
                    3 -> (((phase / (2.0 * PI)) % 1.0) * 2.0 - 1.0).toFloat()
                    else -> Random.nextFloat() * 2f - 1f
                }
                val att = min(1f, i / (SR * 0.008f))
                val env = att * (1f - t).pow(3)
                buf[start + i] += w * env * n.vol
            }
        }
        return ShortArray(total) { (buf[it].coerceIn(-1f, 1f) * 30000f).toInt().toShort() }
    }

    fun play(ctx: Context, st: AppStyle) {
        try {
            val uri = st.soundUri
            if (uri != null) { playUri(ctx, Uri.parse(uri), st.volume); return }
            val data = cache.getOrPut(st.sound) { render(recipes[st.sound] ?: emptyList()) }
            if (data.isEmpty()) return
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SR).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC).build()
            track.write(data, 0, data.size)
            track.setVolume(st.volume)
            track.play()
            main.postDelayed({ try { track.release() } catch (e: Exception) { } }, data.size * 1000L / SR + 400L)
        } catch (e: Exception) { }
    }

    private fun playUri(ctx: Context, uri: Uri, vol: Float) {
        try {
            val mp = MediaPlayer()
            mp.setDataSource(ctx, uri)
            mp.setVolume(vol, vol)
            mp.setOnPreparedListener { it.start(); main.postDelayed({ try { it.stop() } catch (e: Exception) { } }, 4000L) }
            mp.setOnCompletionListener { it.release() }
            mp.setOnErrorListener { p, _, _ -> p.release(); true }
            mp.prepareAsync()
        } catch (e: Exception) { }
    }
}
