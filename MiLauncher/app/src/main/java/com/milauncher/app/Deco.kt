package com.milauncher.app

import androidx.compose.ui.graphics.Path
import kotlin.math.*

private val PIF = PI.toFloat()

/** Elementos dibujables en un lienzo virtual de 324 x 57 (igual que la simulación). */
sealed class El(val a: Float)
class Poly(val p: FloatArray, a: Float) : El(a)
class Disc(val x: Float, val y: Float, val r: Float, a: Float, val ring: Float = 0f, val white: Boolean = false) : El(a)
class Strk(val p: FloatArray, val w: Float, a: Float) : El(a)

private fun Path.addPts(p: FloatArray, closed: Boolean) {
    if (p.size < 4) return
    moveTo(p[0], p[1])
    var i = 2
    while (i + 1 < p.size) { lineTo(p[i], p[i + 1]); i += 2 }
    if (closed) close()
}

private fun midX(p: FloatArray): Float {
    var mn = Float.MAX_VALUE; var mx = -Float.MAX_VALUE; var i = 0
    while (i < p.size) { mn = min(mn, p[i]); mx = max(mx, p[i]); i += 2 }
    return (mn + mx) / 2f
}

/** Elemento "compilado": el Path se crea una sola vez y se reutiliza en cada frame. */
class CEl(val el: El) {
    val path: Path? = when (el) {
        is Poly -> Path().also { it.addPts(el.p, true) }
        is Strk -> Path().also { it.addPts(el.p, false) }
        is Disc -> null
    }
    val cx: Float = when (el) {
        is Poly -> midX(el.p)
        is Strk -> midX(el.p)
        is Disc -> el.x
    }
}

class B {
    val l = ArrayList<El>()
    fun p(a: Float, vararg v: Float) { l.add(Poly(v, a)) }
    fun r(x: Float, y: Float, w: Float, h: Float, a: Float = 1f, skew: Float = 0f) {
        val t = tan(skew * PIF / 180f)
        l.add(Poly(floatArrayOf(x + y * t, y, x + w + y * t, y, x + w + (y + h) * t, y + h, x + (y + h) * t, y + h), a))
    }
    fun rr(cx: Float, cy: Float, w: Float, h: Float, deg: Float, a: Float) {
        val t = deg * PIF / 180f; val c = cos(t); val s = sin(t)
        val xs = floatArrayOf(-w / 2f, w / 2f, w / 2f, -w / 2f); val ys = floatArrayOf(-h / 2f, -h / 2f, h / 2f, h / 2f)
        val o = FloatArray(8)
        for (i in 0 until 4) { o[2 * i] = cx + xs[i] * c - ys[i] * s; o[2 * i + 1] = cy + xs[i] * s + ys[i] * c }
        l.add(Poly(o, a))
    }
    fun d(x: Float, y: Float, rad: Float, a: Float = 1f) { l.add(Disc(x, y, rad, a)) }
    fun o(x: Float, y: Float, rad: Float, a: Float = 1f) { l.add(Disc(x, y, rad, a, ring = 3f)) }
    fun ln(w: Float, a: Float, vararg v: Float) { l.add(Strk(v, w, a)) }
    fun ng(cx: Float, cy: Float, rad: Float, n: Int, rot: Float, a: Float) {
        val o = FloatArray(n * 2)
        for (i in 0 until n) { val t = rot + i * 2f * PIF / n; o[2 * i] = cx + rad * cos(t); o[2 * i + 1] = cy + rad * sin(t) }
        l.add(Poly(o, a))
    }
    fun st(cx: Float, cy: Float, rad: Float, a: Float) {
        val o = FloatArray(20)
        for (i in 0 until 10) {
            val t = -PIF / 2f + i * PIF / 5f; val rr = if (i % 2 == 1) rad * .32f else rad
            o[2 * i] = cx + rr * cos(t); o[2 * i + 1] = cy + rr * sin(t)
        }
        l.add(Poly(o, a))
    }
    fun sp(x: Float, y: Float, rad: Float, a: Float) {
        l.add(Poly(floatArrayOf(x, y - rad, x + .2f * rad, y - .2f * rad, x + rad, y, x + .2f * rad, y + .2f * rad,
            x, y + rad, x - .2f * rad, y + .2f * rad, x - rad, y, x - .2f * rad, y - .2f * rad), a))
    }
    fun ht(x: Float, y: Float, s: Float, a: Float) {
        val n = 40; val k = s * .84f; val o = FloatArray(n * 2)
        for (i in 0 until n) {
            val t = 2f * PIF * i / n
            val hx = 16f * sin(t).pow(3)
            val hy = -(13f * cos(t) - 5f * cos(2f * t) - 2f * cos(3f * t) - cos(4f * t))
            o[2 * i] = x + hx * k; o[2 * i + 1] = y + 6f * s + (hy - 2.5f) * k
        }
        l.add(Poly(o, a))
    }
    fun mn(x: Float, y: Float, s: Float, a: Float) {
        val pts = ArrayList<Float>()
        var dg = 286.7f
        while (dg >= 73.3f) { val t = dg * PIF / 180f; pts.add(x + s * (-6.9f + 24f * cos(t))); pts.add(y + s * (29f + 24f * sin(t))); dg -= 12f }
        dg = 117.7f
        while (dg <= 242.3f) { val t = dg * PIF / 180f; pts.add(x + s * (12.1f + 26f * cos(t))); pts.add(y + s * (29f + 26f * sin(t))); dg += 12f }
        l.add(Poly(pts.toFloatArray(), a))
    }
    fun dr(x: Float, y: Float, s: Float, a: Float) {
        val pts = ArrayList<Float>(); pts.add(x); pts.add(y - 14f * s)
        var dg = -25f
        while (dg <= 205f) { val t = dg * PIF / 180f; pts.add(x + 9f * s * cos(t)); pts.add(y + s * (4f + 9f * sin(t))); dg += 15f }
        l.add(Poly(pts.toFloatArray(), a))
    }
    fun lf(x: Float, y: Float, s: Float, deg: Float, a: Float) {
        val t = deg * PIF / 180f; val c = cos(t); val sv = sin(t)
        val pts = ArrayList<Float>()
        fun add(px: Float, py: Float) { pts.add(x + s * (px * c - py * sv)); pts.add(y + s * (px * sv + py * c)) }
        for (i in 0..8) { val u = i / 8f; add(30f * u, -9f * sin(u * PIF)) }
        for (i in 8 downTo 0) { val u = i / 8f; add(30f * u, 9f * sin(u * PIF)) }
        l.add(Poly(pts.toFloatArray(), a))
    }
    fun cl(x: Float, y: Float, s: Float, a: Float) {
        d(x - 8f * s, y + 2f * s, 6f * s, a); d(x, y - 3f * s, 8f * s, a); d(x + 9f * s, y + s, 6.5f * s, a)
        r(x - 14f * s, y + 2f * s, 28f * s, 6f * s, a)
    }
    fun nt(x: Float, y: Float, s: Float, a: Float) {
        d(x, y, 4.5f * s, a); r(x + 3.4f * s, y - 17f * s, 2f * s, 17f * s, a)
        p(a, x + 5.4f * s, y - 17f * s, x + 12f * s, y - 11f * s, x + 5.4f * s, y - 12f * s)
    }
    fun cr(x: Float, y: Float, rad: Float, a: Float) {
        r(x - rad / 3f, y - rad, rad * 2f / 3f, rad * 2f, a); r(x - rad, y - rad / 3f, rad * 2f, rad * 2f / 3f, a)
    }
    fun fl(cx: Float, cy: Float, rad: Float, a: Float, n: Int) {
        for (i in 0 until n) {
            val t = i * 2f * PIF / n; val c = cos(t); val s = sin(t); val x1 = rad * .5f; val y1 = rad * .26f
            p(a, cx, cy, cx + x1 * c + y1 * s, cy + x1 * s - y1 * c, cx + rad * c, cy + rad * s, cx + x1 * c - y1 * s, cy + x1 * s + y1 * c)
        }
        l.add(Disc(cx, cy, rad * .16f, .9f, white = true))
    }
    fun sn(cx: Float, cy: Float, rad: Float, a: Float) {
        d(cx, cy, rad, a)
        for (i in 0 until 12) {
            val t = i * PIF / 6f; val a1 = t - .13f; val b1 = t + .13f
            p(a, cx + rad * 1.25f * cos(a1), cy + rad * 1.25f * sin(a1), cx + rad * 1.95f * cos(t), cy + rad * 1.95f * sin(t),
                cx + rad * 1.25f * cos(b1), cy + rad * 1.25f * sin(b1))
        }
    }
    fun fk(cx: Float, cy: Float, rad: Float, a: Float) {
        for (i in 0 until 3) { val t = i * PIF / 3f; val x = rad * cos(t); val y = rad * sin(t); ln(2.4f, a, cx - x, cy - y, cx + x, cy + y) }
    }
    fun semi(cx: Float, cy: Float, rad: Float, up: Boolean, a: Float) {
        val pts = ArrayList<Float>(); var dg = 0f
        while (dg <= 180f) { val t = dg * PIF / 180f; pts.add(cx + rad * cos(t)); pts.add(if (up) cy - rad * sin(t) else cy + rad * sin(t)); dg += 15f }
        l.add(Poly(pts.toFloatArray(), a))
    }
    fun rows(arr: FloatArray, k: Int, f: (Int, FloatArray) -> Unit) {
        var i = 0; var idx = 0
        while (i + k <= arr.size) { f(idx, arr.copyOfRange(i, i + k)); i += k; idx++ }
    }
}

object Deco {
    private val cache = HashMap<String, List<CEl>>()
    private val defs = HashMap<String, B.() -> Unit>()
    private fun def(n: String, f: B.() -> Unit) { defs[n] = f }

    fun get(name: String): List<CEl> = cache.getOrPut(name) {
        if (name == "none") emptyList() else { val b = B(); defs[name]?.invoke(b); b.l.map { CEl(it) } }
    }

    private fun wave(y0: Float, amp: Float, per: Float, x0: Float, x1: Float): FloatArray {
        val o = ArrayList<Float>(); var x = x0
        while (x <= x1) { o.add(x); o.add(y0 + amp * sin((x - x0) / per * 2f * PIF)); x += 4f }
        return o.toFloatArray()
    }
    private fun zz(a: Float, b: Float): FloatArray {
        val o = ArrayList<Float>(); o.add(0f); o.add(b)
        for (i in 0 until 22) { o.add((i + 1) * 15f); o.add(if (i % 2 == 1) b else a) }
        return o.toFloatArray()
    }
    private fun arc(cx: Float, rad: Float): FloatArray {
        val o = ArrayList<Float>(); var dg = 180f
        while (dg <= 360f) { val t = dg * PIF / 180f; o.add(cx + rad * cos(t)); o.add(56f + rad * sin(t)); dg += 10f }
        return o.toFloatArray()
    }
    private fun spiral(cx: Float, cy: Float, rm: Float, dir: Float): FloatArray {
        val o = ArrayList<Float>(); var t = 0f
        while (t <= 4f * PIF) { val r = 2f + t / (4f * PIF) * rm; o.add(cx + dir * r * cos(t)); o.add(cy + r * sin(t)); t += .25f }
        return o.toFloatArray()
    }

    init {
        def("lineas") { r(0f, 3f, 324f, 5f); r(0f, 12f, 324f, 2f, .6f); r(0f, 43f, 324f, 2f, .6f); r(0f, 49f, 324f, 5f) }
        def("barras") { for (i in 0 until 12) r(i * 27f, 0f, if (i % 3 == 1) 20f else 9f, 57f, floatArrayOf(.6f, 1f, .8f)[i % 3]) }
        def("estrellas") {
            rows(floatArrayOf(18f, 19f, 14f, 1f, 52f, 42f, 10f, .8f, 86f, 15f, 9f, .7f, 122f, 40f, 15f, 1f, 160f, 17f, 12f, .9f, 196f, 43f, 10f, .75f, 232f, 16f, 15f, 1f, 270f, 40f, 11f, .85f, 306f, 18f, 12f, .9f), 4) { _, v -> st(v[0], v[1], v[2], v[3]) }
            rows(floatArrayOf(36f, 30f, 5f, 104f, 28f, 5f, 142f, 12f, 4f, 178f, 30f, 6f, 214f, 28f, 5f, 252f, 26f, 5f, 290f, 48f, 5f), 3) { _, v -> sp(v[0], v[1], v[2], .8f) }
        }
        def("diagonales") { for (i in 0 until 14) r(i * 26f + 10f, 0f, floatArrayOf(14f, 6f, 22f, 6f)[i % 4], 57f, floatArrayOf(1f, .6f, .9f, .6f)[i % 4], -20f) }
        def("flechas") { for (i in 0 until 14) { val x = i * 26f - 8f; ln(9f, .4f + (i % 5) * .15f, x, 2f, x + 20f, 28.5f, x, 55f) } }
        def("picos") {
            for (i in 0 until 19) { val x = i * 18f
                p(if (i % 2 == 1) .7f else 1f, x, 0f, x + 18f, 0f, x + 9f, 17f)
                p(if (i % 2 == 1) 1f else .7f, x, 57f, x + 18f, 57f, x + 9f, 40f) }
            r(0f, 26f, 324f, 5f, .5f)
        }
        def("cristales") {
            val hs = floatArrayOf(44f, 26f, 52f, 34f, 48f, 22f)
            for (i in 0 until 14) { val x = i * 24f - 6f; val h = hs[i % 6]; p(floatArrayOf(1f, .7f, .85f)[i % 3], x, 57f, x + 12f, 57f - h, x + 24f, 57f) }
            val h2 = floatArrayOf(18f, 30f, 12f, 24f)
            for (i in 0 until 10) { val x = i * 34f + 8f; p(.55f, x, 0f, x + 9f, h2[i % 4], x + 18f, 0f) }
        }
        def("ecualizador") { for (i in 0 until 24) { val h = 12f + abs(sin(i * 1.9f)) * 40f; r(i * 13.6f + 1f, (57f - h) / 2f, 8f, h, .55f + .2f * (i % 3)) } }
        def("circulos") {
            rows(floatArrayOf(20f, 32f, 15f, .9f, 52f, 14f, 8f, .7f, 78f, 40f, 11f, .8f, 112f, 22f, 17f, 1f, 146f, 44f, 8f, .7f, 168f, 16f, 7f, .6f, 196f, 34f, 14f, .9f, 232f, 16f, 10f, .75f, 262f, 42f, 13f, .85f, 296f, 24f, 9f, .7f, 316f, 46f, 6f, .8f, 130f, 44f, 3f, .8f), 4) { _, v -> d(v[0], v[1], v[2], v[3]) }
        }
        def("puntos") { for (rw in 0 until 4) for (c in 0 until 24) d(c * 14f + (if (rw % 2 == 1) 7f else 0f) + 6f, 8f + rw * 14f, 2.4f + ((c + rw) % 3) * .7f, .5f + ((c * rw) % 3) * .2f) }
        def("ondas") { ln(4f, 1f, *wave(12f, 6f, 60f, 0f, 324f)); ln(4f, .7f, *wave(28f, 6f, 60f, 0f, 324f)); ln(4f, .45f, *wave(44f, 6f, 60f, 0f, 324f)) }
        def("zigzag") { ln(6f, 1f, *zz(6f, 30f)); ln(4f, .55f, *zz(26f, 52f)) }
        def("corazones") {
            rows(floatArrayOf(16f, 22f, 1f, 1f, 50f, 10f, .8f, .7f, 82f, 26f, 1.2f, 1f, 116f, 8f, .9f, .75f, 150f, 24f, 1.4f, 1f, 190f, 8f, .8f, .7f, 222f, 26f, 1.2f, .95f, 258f, 10f, 1f, .75f, 292f, 24f, 1.3f, 1f), 4) { _, v -> ht(v[0], v[1], v[2], v[3]) }
        }
        def("rombos") {
            rows(floatArrayOf(18f, 29f, 26f, 1f, 52f, 14f, 12f, .7f, 52f, 44f, 12f, .7f, 88f, 29f, 22f, .9f, 124f, 12f, 9f, .6f, 124f, 46f, 9f, .6f, 158f, 29f, 26f, 1f, 196f, 29f, 14f, .7f, 230f, 16f, 10f, .6f, 230f, 42f, 10f, .6f, 262f, 29f, 24f, .95f, 300f, 29f, 16f, .8f), 4) { _, v ->
                p(v[3], v[0], v[1] - v[2], v[0] + v[2] * .55f, v[1], v[0], v[1] + v[2], v[0] - v[2] * .55f, v[1]) }
        }
        def("triangulos") {
            for (i in 0 until 15) { val x = i * 23f
                p(.55f + (i % 3) * .22f, x, 28f, x + 11.5f, 2f, x + 23f, 28f)
                p(.5f + ((i + 1) % 3) * .22f, x + 11.5f, 29f, x + 34.5f, 29f, x + 23f, 55f) }
        }
        def("hexagonos") {
            rows(floatArrayOf(22f, 29f, 22f, 1f, 58f, 14f, 10f, .7f, 60f, 44f, 10f, .7f, 98f, 29f, 16f, .85f, 134f, 15f, 8f, .6f, 138f, 43f, 12f, .8f, 172f, 29f, 24f, 1f, 212f, 15f, 9f, .65f, 210f, 43f, 11f, .75f, 248f, 29f, 20f, .9f, 286f, 16f, 10f, .7f, 288f, 43f, 10f, .8f, 318f, 29f, 14f, .9f), 4) { _, v -> ng(v[0], v[1], v[2], 6, -PIF / 2f, v[3]) }
        }
        def("destellos") {
            rows(floatArrayOf(22f, 29f, 22f, 1f, 56f, 12f, 9f, .75f, 62f, 44f, 11f, .8f, 100f, 26f, 18f, .95f, 136f, 12f, 8f, .7f, 142f, 42f, 13f, .9f, 184f, 29f, 24f, 1f, 222f, 14f, 9f, .7f, 226f, 44f, 11f, .85f, 262f, 28f, 20f, .95f, 296f, 14f, 8f, .7f, 304f, 44f, 12f, .85f), 4) { _, v -> sp(v[0], v[1], v[2], v[3]) }
        }
        def("rayos") { for (i in 0 until 9) { val x = i * 36f + 4f; p(floatArrayOf(1f, .65f, .85f)[i % 3], x + 12f, 1f, x + 1f, 30f, x + 11f, 30f, x + 3f, 56f, x + 26f, 22f, x + 14f, 22f, x + 24f, 1f) } }
        def("luna") {
            mn(52f, 0f, 1f, 1f); mn(200f, 10f, .6f, .8f); st(110f, 16f, 9f, .9f); st(150f, 40f, 13f, 1f); st(256f, 18f, 12f, .95f)
            sp(290f, 40f, 8f, .8f); sp(86f, 46f, 6f, .7f); st(318f, 28f, 6f, .7f); sp(176f, 14f, 6f, .8f); sp(232f, 46f, 7f, .8f)
        }
        def("flores") {
            rows(floatArrayOf(24f, 29f, 22f, 1f, 8f, 70f, 16f, 12f, .8f, 6f, 72f, 44f, 12f, .8f, 6f, 116f, 29f, 18f, .95f, 8f, 162f, 29f, 24f, 1f, 10f, 210f, 15f, 11f, .8f, 6f, 212f, 44f, 12f, .85f, 6f, 254f, 29f, 20f, 1f, 8f, 298f, 17f, 12f, .8f, 6f, 300f, 44f, 12f, .85f, 6f), 5) { _, v -> fl(v[0], v[1], v[2], v[3], v[4].toInt()) }
        }
        def("confeti") {
            for (i in 0 until 22) {
                val x = 8f + i * 14.5f; val y = 8f + ((i * 37) % 41); val rt = ((i * 47) % 90).toFloat(); val al = .55f + (i % 3) * .2f
                if (i % 2 == 1) ng(x, y, 6f, 3, rt * PIF / 180f, al) else rr(x, y, 12f, 4f, rt, al)
            }
        }
        def("arcoiris") {
            val rs = floatArrayOf(46f, 36f, 26f, 16f)
            for (cx in floatArrayOf(54f, 162f, 270f)) for (i in 0 until 4) ln(7f, 1f - i * .18f, *arc(cx, rs[i]))
        }
        def("ajedrez") {
            val w = 324f / 23f; val h = 57f / 4f
            for (rw in 0 until 4) for (c in 0 until 23) if ((rw + c) % 2 == 0) r(c * w, rw * h, w + .3f, h + .3f, .5f + ((rw + c) % 4) * .15f)
        }
        def("gotas") {
            rows(floatArrayOf(20f, 30f, 1.4f, .9f, 50f, 18f, .9f, .7f, 78f, 38f, 1.1f, .8f, 108f, 26f, 1.5f, 1f, 142f, 40f, .9f, .7f, 170f, 22f, 1.2f, .85f, 200f, 34f, 1.6f, 1f, 232f, 20f, 1f, .75f, 262f, 38f, 1.2f, .9f, 296f, 26f, 1.4f, 1f), 4) { _, v -> dr(v[0], v[1], v[2], v[3]) }
        }
        def("nubes") {
            rows(floatArrayOf(30f, 28f, 1.2f, 1f, 80f, 22f, .8f, .7f, 96f, 42f, 1f, .85f, 150f, 26f, 1.3f, 1f, 205f, 40f, .9f, .7f, 230f, 22f, 1.1f, .9f, 285f, 32f, 1.2f, 1f), 4) { _, v -> cl(v[0], v[1], v[2], v[3]) }
        }
        def("anillos") {
            val xs = floatArrayOf(30f, 96f, 160f, 226f, 290f)
            val rs = arrayOf(floatArrayOf(8f, 16f, 24f), floatArrayOf(6f, 13f), floatArrayOf(10f, 19f, 28f), floatArrayOf(7f, 14f, 21f), floatArrayOf(9f, 18f))
            for (k in 0 until 5) for (i in rs[k].indices) o(xs[k], 29f, rs[k][i], 1f - i * .22f)
        }
        def("hojas") {
            rows(floatArrayOf(10f, 40f, 1f, -30f, 40f, 18f, .8f, 20f, 70f, 44f, 1.1f, -50f, 110f, 22f, 1f, 30f, 146f, 48f, .9f, -20f, 176f, 20f, 1.2f, 40f, 214f, 46f, 1f, -40f, 246f, 18f, .9f, 10f, 280f, 44f, 1.1f, -25f), 4) { i, v -> lf(v[0], v[1], v[2], v[3], .6f + (i % 3) * .2f) }
        }
        def("cuadricula") {
            for (i in 0 until 17) r(i * 20f, 0f, 1.6f, 57f, .7f)
            for (y in floatArrayOf(14f, 28f, 42f)) r(0f, y, 324f, 1.6f, .7f)
            r(0f, 0f, 324f, 3f); r(0f, 54f, 324f, 3f)
        }
        def("rayitas") { for (i in 0 until 40) r(i * 9f + 16f, 0f, 2.5f, 57f, .45f + (i % 4) * .15f, -30f) }
        def("constelacion") {
            val c = floatArrayOf(20f, 40f, 52f, 16f, 92f, 34f, 130f, 12f, 166f, 40f, 204f, 18f, 242f, 38f, 280f, 14f, 312f, 34f)
            ln(1.6f, .6f, *c); rows(c, 2) { i, v -> d(v[0], v[1], 3f + (i % 3), 1f) }
            sp(110f, 46f, 5f, .8f); sp(222f, 48f, 5f, .8f); sp(262f, 8f, 4f, .8f)
        }
        def("fugaces") {
            rows(floatArrayOf(70f, 14f, 170f, 30f, 262f, 12f, 130f, 48f, 300f, 44f), 2) { i, v ->
                p(.6f, v[0] - 48f, v[1] - 10f, v[0] - 2f, v[1] - 1.6f, v[0] - 2f, v[1] + 1.6f); st(v[0], v[1], 6f + (i % 2) * 3f, 1f) }
        }
        def("espiral") { ln(3.2f, 1f, *spiral(62f, 29f, 27f, 1f)); ln(3.2f, .7f, *spiral(162f, 29f, 27f, -1f)); ln(3.2f, .9f, *spiral(262f, 29f, 27f, 1f)) }
        def("notas") {
            rows(floatArrayOf(20f, 40f, 1f, 54f, 32f, .8f, 88f, 44f, 1.1f, 126f, 34f, .9f, 160f, 42f, 1.2f, 198f, 30f, .8f, 232f, 44f, 1f, 268f, 34f, 1.1f, 302f, 42f, .9f), 3) { i, v -> nt(v[0], v[1], v[2], .6f + (i % 3) * .2f) }
        }
        def("sol") { sn(44f, 29f, 9f, 1f); sn(150f, 29f, 13f, 1f); sn(262f, 29f, 10f, .9f); st(100f, 14f, 6f, .8f); st(206f, 44f, 6f, .8f); sp(304f, 14f, 6f, .8f) }
        def("copos") {
            rows(floatArrayOf(30f, 29f, 16f, 1f, 80f, 16f, 9f, .7f, 84f, 44f, 9f, .7f, 130f, 29f, 14f, .95f, 180f, 18f, 10f, .75f, 216f, 42f, 12f, .85f, 262f, 29f, 16f, 1f, 300f, 18f, 8f, .7f), 4) { _, v -> fk(v[0], v[1], v[2], v[3]) }
        }
        def("cruces") {
            rows(floatArrayOf(16f, 20f, 8f, 46f, 42f, 6f, 78f, 16f, 5f, 104f, 34f, 9f, 140f, 14f, 6f, 170f, 40f, 8f, 200f, 20f, 7f, 234f, 42f, 6f, 262f, 18f, 9f, 296f, 38f, 7f, 316f, 14f, 5f), 3) { i, v -> cr(v[0], v[1], v[2], .55f + (i % 3) * .22f) }
        }
        def("olas") {
            for (i in 0 until 15) { val x = i * 23f; semi(x + 11.5f, 57f, 11.5f, true, if (i % 2 == 1) .7f else 1f); semi(x + 11.5f, 0f, 11.5f, false, if (i % 2 == 1) 1f else .7f) }
            r(0f, 26f, 324f, 5f, .5f)
        }
    }
}

val DECO_NAMES = listOf("none" to "Ninguna", "lineas" to "Líneas", "barras" to "Barras", "estrellas" to "Estrellas", "diagonales" to "Diagonales",
    "flechas" to "Flechas", "picos" to "Picos", "cristales" to "Cristales", "ecualizador" to "Ecualizador", "circulos" to "Círculos",
    "puntos" to "Puntos", "ondas" to "Ondas", "zigzag" to "Zigzag", "corazones" to "Corazones", "rombos" to "Rombos",
    "triangulos" to "Triángulos", "hexagonos" to "Hexágonos", "destellos" to "Destellos", "rayos" to "Rayos", "luna" to "Luna",
    "flores" to "Flores", "confeti" to "Confeti", "arcoiris" to "Arcoíris", "ajedrez" to "Ajedrez", "gotas" to "Gotas",
    "nubes" to "Nubes", "anillos" to "Anillos", "hojas" to "Hojas", "cuadricula" to "Cuadrícula", "rayitas" to "Rayitas",
    "constelacion" to "Constelación", "fugaces" to "Fugaces", "espiral" to "Espirales", "notas" to "Notas", "sol" to "Soles",
    "copos" to "Copos", "cruces" to "Cruces", "olas" to "Olas")
