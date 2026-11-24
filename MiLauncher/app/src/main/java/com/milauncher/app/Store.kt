package com.milauncher.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import kotlin.math.min

/** Guarda estilos, ajustes globales, iconos propios y fuentes importadas. */
class Store(private val ctx: Context) {
    private val sp = ctx.getSharedPreferences("styles", Context.MODE_PRIVATE)
    private val gp = ctx.getSharedPreferences("global", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val pending = HashMap<String, AppStyle>()
    private val iconCache = HashMap<String, ImageBitmap>()
    private val flush = Runnable {
        val e = sp.edit()
        for ((k, v) in pending) e.putString(k, v.toJson())
        pending.clear()
        e.apply()
    }

    /** Guardado con retardo: mientras arrastras un deslizador no se escribe en cada movimiento. */
    fun saveLater(pkg: String, s: AppStyle) { pending[pkg] = s; handler.removeCallbacks(flush); handler.postDelayed(flush, 350L) }
    fun flushNow() { handler.removeCallbacks(flush); flush.run() }

    fun loadAll(): Map<String, AppStyle> {
        val m = HashMap<String, AppStyle>()
        for ((k, v) in sp.all) if (v is String) m[k] = styleFromJson(v)
        return m
    }

    fun clearAll() {
        pending.clear(); sp.edit().clear().apply()
        File(ctx.filesDir, "icons").deleteRecursively(); iconCache.clear()
    }

    var soundOn: Boolean
        get() = gp.getBoolean("snd", true)
        set(v) { gp.edit().putBoolean("snd", v).apply() }
    var glowOn: Boolean
        get() = gp.getBoolean("glow", true)
        set(v) { gp.edit().putBoolean("glow", v).apply() }
    var seqStep: Int
        get() = gp.getInt("step", 1200)
        set(v) { gp.edit().putInt("step", v).apply() }
    var entrance: String
        get() = gp.getString("entrance", "inL") ?: "inL"
        set(v) { gp.edit().putString("entrance", v).apply() }

    private fun iconFile(pkg: String) = File(File(ctx.filesDir, "icons"), "$pkg.png")

    fun saveIcon(pkg: String, uri: Uri): Boolean {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var s = 1
            while (bounds.outWidth / (s * 2) >= 320 && bounds.outHeight / (s * 2) >= 320) s *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = s }
            val bmp = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return false
            val m = min(bmp.width, bmp.height)
            val sq = Bitmap.createBitmap(bmp, (bmp.width - m) / 2, (bmp.height - m) / 2, m, m)
            val out = Bitmap.createScaledBitmap(sq, 192, 192, true)
            val f = iconFile(pkg)
            f.parentFile?.mkdirs()
            f.outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
            iconCache[pkg] = out.asImageBitmap()
            return true
        } catch (e: Exception) { return false }
    }

    fun loadIcon(pkg: String): ImageBitmap? {
        iconCache[pkg]?.let { return it }
        val f = iconFile(pkg)
        if (!f.exists()) return null
        val b = BitmapFactory.decodeFile(f.path) ?: return null
        val im = b.asImageBitmap()
        iconCache[pkg] = im
        return im
    }

    fun displayName(uri: Uri): String {
        var n: String? = null
        try {
            ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (i >= 0) n = c.getString(i) }
            }
        } catch (e: Exception) { }
        return n ?: (uri.lastPathSegment ?: "archivo")
    }

    /** Copia una fuente elegida por el usuario y devuelve su clave, o null si falló. */
    fun importFont(uri: Uri): String? {
        try {
            val name = displayName(uri).replace(Regex("[^A-Za-z0-9._-]"), "_")
            if (!(name.endsWith(".ttf", true) || name.endsWith(".otf", true))) return null
            val dir = File(ctx.filesDir, "fonts"); dir.mkdirs()
            val f = File(dir, name)
            ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } } ?: return null
            Typeface.createFromFile(f)
            Fonts.load(ctx)
            return "f:$name"
        } catch (e: Exception) { return null }
    }
}
