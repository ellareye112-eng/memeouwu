package com.milauncher.app

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private val PIF = PI.toFloat()

class PolyShape(private val pts: FloatArray) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val p = Path()
        p.moveTo(pts[0] * size.width, pts[1] * size.height)
        var i = 2
        while (i + 1 < pts.size) { p.lineTo(pts[i] * size.width, pts[i + 1] * size.height); i += 2 }
        p.close()
        return Outline.Generic(p)
    }
}

private object OvalShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val p = Path()
        p.addOval(Rect(0f, size.height * .14f, size.width, size.height * .86f))
        return Outline.Generic(p)
    }
}

object IconShapes {
    class Def(val key: String, val label: String, val pts: FloatArray?)

    private fun pct(vararg v: Float) = FloatArray(v.size) { v[it] / 100f }
    private fun reg(n: Int, rot: Float) = FloatArray(n * 2).also { o ->
        for (i in 0 until n) { val t = rot + i * 2f * PIF / n; o[2 * i] = .5f + .5f * cos(t); o[2 * i + 1] = .5f + .5f * sin(t) } }
    private fun starP(n: Int, k: Float) = FloatArray(n * 4).also { o ->
        for (i in 0 until n * 2) { val t = -PIF / 2f + i * PIF / n; val r = if (i % 2 == 1) .5f * k else .5f
            o[2 * i] = .5f + r * cos(t); o[2 * i + 1] = .5f + r * sin(t) } }
    private fun heartP() = FloatArray(96).also { o ->
        for (i in 0 until 48) { val t = i / 48f * 2f * PIF
            o[2 * i] = .5f + 16f * sin(t).pow(3) * .029f
            o[2 * i + 1] = .48f - (13f * cos(t) - 5f * cos(2f * t) - 2f * cos(3f * t) - cos(4f * t)) * .029f } }
    private fun flowerP() = FloatArray(144).also { o ->
        for (i in 0 until 72) { val t = i / 72f * 2f * PIF; val r = .44f + .05f * cos(8f * t)
            o[2 * i] = .5f + r * cos(t); o[2 * i + 1] = .5f + r * sin(t) } }
    private fun dropP(): FloatArray {
        val l = ArrayList<Float>(); l.add(.5f); l.add(0f)
        var d = -30f
        while (d <= 210f) { val t = d * PIF / 180f; l.add(.5f + .36f * cos(t)); l.add(.64f + .36f * sin(t)); d += 15f }
        return l.toFloatArray()
    }
    private fun moonP(): FloatArray {
        val l = ArrayList<Float>(); var d = 90f
        while (d <= 270f) { val t = d * PIF / 180f; l.add(.55f + .48f * cos(t)); l.add(.5f + .48f * sin(t)); d += 15f }
        d = 270f
        while (d >= 90f) { val t = d * PIF / 180f; l.add(.68f + .36f * cos(t)); l.add(.5f + .36f * sin(t)); d -= 15f }
        return l.toFloatArray()
    }
    private fun archP(): FloatArray {
        val l = ArrayList<Float>(); var d = 180f
        while (d <= 360f) { val t = d * PIF / 180f; l.add(.5f + .5f * cos(t)); l.add(.5f + .5f * sin(t)); d += 15f }
        l.add(1f); l.add(1f); l.add(0f); l.add(1f)
        return l.toFloatArray()
    }

    val defs: List<Def> = listOf(
        Def("rect", "Rectángulo", null), Def("redondeado", "Redondeado", null), Def("circulo", "Círculo", null), Def("ovalo", "Óvalo", null),
        Def("hexagono", "Hexágono", reg(6, -PIF / 2f)), Def("octagono", "Octágono", reg(8, PIF / 8f)),
        Def("rombo", "Rombo", pct(50f, 0f, 100f, 50f, 50f, 100f, 0f, 50f)), Def("diamante", "Diamante", pct(50f, 0f, 85f, 50f, 50f, 100f, 15f, 50f)),
        Def("triangulo", "Triángulo", pct(50f, 4f, 100f, 92f, 0f, 92f)), Def("pentagono", "Pentágono", reg(5, -PIF / 2f)),
        Def("estrella", "Estrella", starP(5, .4f)), Def("estrella6", "Estrella 6", starP(6, .5f)),
        Def("sello", "Sello", starP(8, .78f)), Def("sello12", "Sello 12", starP(12, .82f)),
        Def("corazon", "Corazón", heartP()), Def("escudo", "Escudo", pct(0f, 0f, 100f, 0f, 100f, 55f, 50f, 100f, 0f, 55f)),
        Def("cruz", "Cruz", pct(33f, 0f, 67f, 0f, 67f, 33f, 100f, 33f, 100f, 67f, 67f, 67f, 67f, 100f, 33f, 100f, 33f, 67f, 0f, 67f, 0f, 33f, 33f, 33f)),
        Def("flor", "Flor", flowerP()), Def("paralelogramo", "Paralelogramo", pct(20f, 0f, 100f, 0f, 80f, 100f, 0f, 100f)),
        Def("trapecio", "Trapecio", pct(20f, 0f, 80f, 0f, 100f, 100f, 0f, 100f)), Def("etiqueta", "Etiqueta", pct(0f, 0f, 76f, 0f, 100f, 24f, 100f, 100f, 0f, 100f)),
        Def("rayo", "Rayo", pct(60f, 0f, 22f, 54f, 46f, 54f, 38f, 100f, 80f, 40f, 56f, 40f, 72f, 0f)),
        Def("casa", "Casa", pct(50f, 0f, 100f, 45f, 86f, 45f, 86f, 100f, 14f, 100f, 14f, 45f, 0f, 45f)),
        Def("gema", "Gema", pct(25f, 0f, 75f, 0f, 100f, 35f, 50f, 100f, 0f, 35f)),
        Def("flecha", "Flecha", pct(0f, 35f, 55f, 35f, 55f, 10f, 100f, 50f, 55f, 90f, 55f, 65f, 0f, 65f)),
        Def("gota", "Gota", dropP()), Def("luna", "Luna", moonP()), Def("arco", "Arco", archP()),
        Def("chat", "Chat", pct(0f, 0f, 100f, 0f, 100f, 70f, 60f, 70f, 30f, 100f, 35f, 70f, 0f, 70f))
    )
    val options: List<Pair<String, String>> = defs.map { it.key to it.label }

    private val cache = HashMap<String, Shape>()
    fun shape(key: String): Shape = cache.getOrPut(key) {
        when (key) {
            "rect" -> RectangleShape
            "redondeado" -> RoundedCornerShape(28)
            "circulo" -> CircleShape
            "ovalo" -> OvalShape
            else -> defs.firstOrNull { it.key == key }?.pts?.let { PolyShape(it) } ?: RoundedCornerShape(28)
        }
    }
    fun points(key: String): FloatArray? = defs.firstOrNull { it.key == key }?.pts
}
