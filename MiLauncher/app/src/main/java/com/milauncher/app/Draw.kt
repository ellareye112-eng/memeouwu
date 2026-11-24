package com.milauncher.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private val PIF = PI.toFloat()

fun mix(c: Color, k: Float): Color {
    val b = floatArrayOf(42f, 34f, 32f)
    return Color((b[0] * (1 - k) + c.red * 255f * k) / 255f, (b[1] * (1 - k) + c.green * 255f * k) / 255f, (b[2] * (1 - k) + c.blue * 255f * k) / 255f)
}
fun pastel(c: Color) = Color(c.red + (1f - c.red) * .55f, c.green + (1f - c.green) * .55f, c.blue + (1f - c.blue) * .55f)

private fun flashCurve(dt: Float): Float = if (dt < 0f || dt > 1f) 0f else if (dt < .35f) dt / .35f else (1f - dt) / .65f

private fun glowAmt(mode: String, i: Int, now: Long, rg: RowGlow): Float = when (mode) {
    "always" -> (0.5f + 0.5f * sin(2f * PIF * ((now % 3200L).toFloat() / 3200f - i * 0.115f))) * .8f
    "breath" -> 0.5f + 0.5f * sin(2f * PIF * ((now % 4000L).toFloat() / 4000f))
    "blink" -> {
        val per = 800L + (i * 137L) % 900L
        0.5f + 0.5f * sin(2f * PIF * ((now % per).toFloat() / per + (i * 311 % 1000) / 1000f))
    }
    "random", "wave", "seq" -> if (i >= rg.flash.size) 0f else flashCurve((now - rg.flash[i]) / 1000f)
    else -> 0f
}

val DUMMY_GLOW = RowGlow("")

/** Dibuja la decoración ocupando toda la barra (escala "cubrir", centrada) con brillo opcional. */
fun DrawScope.drawDeco(rg: RowGlow, els: List<CEl>, mode: String, glowOn: Boolean, color: Color, now: Long, alpha: Float = .88f) {
    if (els.isEmpty()) return
    val s = max(size.width / 324f, size.height / 57f)
    val tx = (size.width - 324f * s) / 2f
    val ty = (size.height - 57f * s) / 2f
    var base = color
    if (glowOn && mode == "rainbow") {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.hashColor(), hsv)
        hsv[0] = (hsv[0] + (now % 6000L) / 6000f * 360f) % 360f
        base = Color(android.graphics.Color.HSVToColor(hsv))
    }
    withTransform({ translate(tx, ty); scale(s, s, Offset.Zero) }) {
        for (i in els.indices) {
            val c = els[i]; val e = c.el
            val g = if (glowOn) glowAmt(mode, i, now, rg) else 0f
            val col = if (g > 0f) lerp(base, Color.White, g * .45f) else base
            val al = (e.a + (1f - e.a) * g) * alpha
            when (e) {
                is Poly -> drawPath(c.path!!, col.copy(alpha = al))
                is Strk -> drawPath(c.path!!, col.copy(alpha = al), style = Stroke(width = e.w, miter = 10f, cap = StrokeCap.Butt, join = StrokeJoin.Miter))
                is Disc -> {
                    if (e.white) drawCircle(Color.White.copy(alpha = .9f), e.r, Offset(e.x, e.y))
                    else if (e.ring > 0f) drawCircle(col.copy(alpha = al), e.r, Offset(e.x, e.y), style = Stroke(width = e.ring))
                    else drawCircle(col.copy(alpha = al), e.r, Offset(e.x, e.y))
                }
            }
        }
    }
}

private fun Color.hashColor(): Int = android.graphics.Color.argb(255, (red * 255f).toInt(), (green * 255f).toInt(), (blue * 255f).toInt())

fun pulseAlpha(now: Long, rg: RowGlow): Float {
    val pd = (now - rg.pulse) / 1000f
    return if (pd < 0f || pd > 1f) 0f else .13f * (if (pd < .3f) pd / .3f else (1f - pd) / .7f)
}

/* ---------------- partículas al tocar ---------------- */

class Particle(val kind: String, val x: Float, val y: Float, val dx: Float, val dy: Float, val rot: Float,
               val size: Float, val color: Color, val start: Long)

private val unitPaths = HashMap<String, Path>()
private fun unitPath(kind: String): Path = unitPaths.getOrPut(kind) {
    val pts: FloatArray? = when (kind) {
        "spark" -> floatArrayOf(.5f, 0f, .6f, .4f, 1f, .5f, .6f, .6f, .5f, 1f, .4f, .6f, 0f, .5f, .4f, .4f)
        "estrella" -> IconShapes.points("estrella")
        "corazon" -> IconShapes.points("corazon")
        "flor" -> IconShapes.points("flor")
        "rombo" -> IconShapes.points("rombo")
        "triangulo" -> IconShapes.points("triangulo")
        "rect" -> floatArrayOf(0f, .25f, 1f, .25f, 1f, .75f, 0f, .75f)
        else -> null
    }
    val p = Path()
    if (pts == null) p.addOval(androidx.compose.ui.geometry.Rect(-.5f, -.5f, .5f, .5f))
    else {
        p.moveTo(pts[0] - .5f, pts[1] - .5f)
        var i = 2
        while (i + 1 < pts.size) { p.lineTo(pts[i] - .5f, pts[i + 1] - .5f); i += 2 }
        p.close()
    }
    p
}

fun spawnParticles(list: MutableList<Particle>, st: AppStyle, p: Offset, auto: Color, d: Float) {
    if (st.tap == "none") return
    val now = Anim.now(); Anim.need(1300L)
    val col0 = st.decoColor?.let { Color(it) } ?: pastel(st.barColor?.let { Color(it) } ?: auto)
    list.removeAll { now - it.start > 1000L }
    if (st.tap == "onda") {
        for (k in 0..1) list.add(Particle("onda", p.x, p.y, 0f, 0f, 0f, 4f * d, col0, now + k * 180L))
        return
    }
    val n = when (st.amt) { 1 -> 5; 3 -> 15; else -> 9 }
    for (i in 0 until n) {
        val a = Random.nextFloat() * 2f * PIF
        val r = (18f + Random.nextFloat() * 34f) * d
        var kind = when (st.tap) {
            "destellos" -> "spark"; "estrellas" -> "estrella"; "corazones" -> "corazon"; "puntos" -> "dot"; "flores" -> "flor"
            "rombos" -> "rombo"; "confeti" -> listOf("triangulo", "rombo", "rect")[i % 3]; "burbujas" -> "burbuja"; "chispas" -> "chispa"
            else -> "dot"
        }
        var col = col0
        if (st.tap == "confeti") col = CONFETTI[Random.nextInt(CONFETTI.size)]
        val size = (5f + Random.nextFloat() * 8f) * d
        val rot = if (kind == "chispa") a * 180f / PIF + 90f else (Random.nextFloat() - .5f) * 140f
        list.add(Particle(kind, p.x, p.y, cos(a) * r, sin(a) * r - 8f * d, rot, size, col, now + (Random.nextFloat() * 80f).toLong()))
    }
}

fun DrawScope.drawParticles(parts: List<Particle>, now: Long) {
    for (p in parts) {
        val t = (now - p.start) / 800f
        if (t < 0f || t > 1f) continue
        val e = 1f - (1f - t) * (1f - t)
        val al = 1f - t
        val cx = p.x + p.dx * e; val cy = p.y + p.dy * e
        when (p.kind) {
            "onda" -> drawCircle(p.color.copy(alpha = al * .9f), p.size * (.3f + 13.7f * e), Offset(p.x, p.y), style = Stroke(width = 2f * (p.size / 4f)))
            "burbuja" -> drawCircle(p.color.copy(alpha = al), p.size * (.4f + .6f * t), Offset(cx, cy), style = Stroke(width = 1.5f * (p.size / 6f)))
            "chispa" -> withTransform({ translate(cx, cy); rotate(p.rot, Offset.Zero) }) {
                drawLine(p.color.copy(alpha = al), Offset(0f, -p.size / 2f), Offset(0f, p.size / 2f), strokeWidth = 2f * (p.size / 8f))
            }
            else -> {
                val sc = p.size * (.3f + .8f * e)
                withTransform({ translate(cx, cy); rotate(p.rot * e, Offset.Zero); scale(sc, sc, Offset.Zero) }) {
                    drawPath(unitPath(p.kind), p.color.copy(alpha = al))
                }
            }
        }
    }
}

/** Orden en que se encienden las piezas de una barra (ola / secuencia). */
fun dirOrder(els: List<CEl>, dir: String): List<Int> {
    val idx = els.indices.toMutableList()
    when (dir) {
        "rl" -> idx.sortByDescending { els[it].cx }
        "center" -> idx.sortBy { kotlin.math.abs(els[it].cx - 162f) }
        "rand" -> idx.shuffle()
        else -> idx.sortBy { els[it].cx }
    }
    return idx
}

fun sweep(rg: RowGlow, spanMs: Float, pulse: Boolean) {
    val n = rg.els.size
    if (n == 0 || rg.flash.size != n) return
    val now = Anim.now()
    dirOrder(rg.els, rg.dir).forEachIndexed { k, ei -> rg.flash[ei] = now + (k * spanMs / n).toLong() }
    if (pulse) rg.pulse = now
    Anim.need((spanMs + 1300f).toLong())
}
