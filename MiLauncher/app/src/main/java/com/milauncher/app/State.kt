package com.milauncher.app

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Reloj de animación bajo demanda: solo pide frames cuando hay algo animándose (ahorra batería). */
object Anim {
    private val t0 = SystemClock.uptimeMillis()
    val time = mutableLongStateOf(0L)
    @Volatile var until = 0L
    fun now(): Long = SystemClock.uptimeMillis() - t0
    fun need(ms: Long) { val n = now() + ms; if (n > until) until = n }
}

/** Estado de brillo de una barra visible: instantes de inicio del destello de cada pieza. */
class RowGlow(val pkg: String) {
    var idx = 0
    var mode = "none"
    var order: Int? = null
    var dir = "lr"
    var els: List<CEl> = emptyList()
    var flash = LongArray(0)
    var pulse = -100000L
    val continuous: Boolean get() = mode == "always" || mode == "breath" || mode == "blink" || mode == "rainbow"
}

object Registry { val rows = HashMap<String, RowGlow>() }

class LauncherState(val store: Store) {
    val styles = mutableStateMapOf<String, AppStyle>().also { it.putAll(store.loadAll()) }
    val iconVer = mutableStateMapOf<String, Int>()
    var soundOn by mutableStateOf(store.soundOn)
    var glowOn by mutableStateOf(store.glowOn)
    var seqStep by mutableStateOf(store.seqStep)
    var entrance by mutableStateOf(store.entrance)
    var replayKey by mutableIntStateOf(0)
    var homeTick by mutableIntStateOf(0)
    var fontVer by mutableIntStateOf(0)
    var active by mutableStateOf(true)
    val played = HashSet<String>()

    fun style(a: AppEntry): AppStyle = styles[a.pkg] ?: defaultFor(a.label)

    fun update(a: AppEntry, f: (AppStyle) -> AppStyle) {
        val n = f(style(a)); styles[a.pkg] = n; store.saveLater(a.pkg, n)
    }

    fun copyLook(apps: List<AppEntry>, from: AppEntry) {
        val src = style(from)
        for (a in apps) if (a.pkg != from.pkg) update(a) { it.withLookOf(src) }
    }

    fun applyTheme(t: Theme, apps: List<AppEntry>) {
        apps.forEachIndexed { i, a ->
            update(a) {
                it.copy(barColor = t.bars[i % t.bars.size], decoColor = null, deco = t.shapes[i % t.shapes.size], font = t.font,
                    fontScale = 1f, iconShape = t.iconShape, frame = t.frame, frameColor = null, iconBg = null,
                    glow = t.glow, barStyle = t.barStyle)
            }
        }
    }

    fun resetAll(apps: List<AppEntry>) {
        store.clearAll(); styles.clear(); iconVer.keys.toList().forEach { iconVer[it] = (iconVer[it] ?: 0) + 1 }
    }

    fun setSound(v: Boolean) { soundOn = v; store.soundOn = v }
    fun setGlow(v: Boolean) { glowOn = v; store.glowOn = v }
    fun setStep(v: Int) { seqStep = v; store.seqStep = v }
    fun chooseEntrance(v: String) { entrance = v; store.entrance = v; replayKey++ }
}
