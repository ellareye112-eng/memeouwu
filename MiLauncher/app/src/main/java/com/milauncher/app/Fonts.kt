package com.milauncher.app

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import java.io.File

class FontOpt(val key: String, val label: String, val family: FontFamily)

/**
 * Fuentes: las del sistema + cualquier .ttf/.otf que pongas en app/src/main/assets/fonts
 * + las que importes desde el teléfono (se guardan en el almacenamiento de la app).
 */
object Fonts {
    var all: List<FontOpt> = emptyList()
        private set

    private val SYSTEM = listOf(
        "sans-serif" to "Sans", "sans-serif-light" to "Sans ligera", "sans-serif-thin" to "Sans fina",
        "sans-serif-medium" to "Sans media", "sans-serif-black" to "Sans negra", "sans-serif-condensed" to "Condensada",
        "sans-serif-smallcaps" to "Versalitas", "serif" to "Serif", "monospace" to "Monoespaciada",
        "serif-monospace" to "Serif mono", "cursive" to "Cursiva", "casual" to "Casual"
    )

    fun load(ctx: Context) {
        val l = ArrayList<FontOpt>()
        l.add(FontOpt("def", "Predeterminada", FontFamily.Default))
        for ((k, n) in SYSTEM) l.add(FontOpt(k, n, FontFamily(Typeface.create(k, Typeface.NORMAL))))
        l.add(FontOpt("bi", "Negrita cursiva", FontFamily(Typeface.create("sans-serif", Typeface.BOLD_ITALIC))))
        try {
            val names = ctx.assets.list("fonts")
            if (names != null) for (f in names.sorted()) {
                if (f.endsWith(".ttf", true) || f.endsWith(".otf", true)) {
                    try { l.add(FontOpt("a:$f", f.substringBeforeLast('.'), FontFamily(Typeface.createFromAsset(ctx.assets, "fonts/$f")))) } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) { }
        val files = File(ctx.filesDir, "fonts").listFiles()
        if (files != null) for (f in files.sortedBy { it.name }) {
            try { l.add(FontOpt("f:${f.name}", f.nameWithoutExtension, FontFamily(Typeface.createFromFile(f)))) } catch (e: Exception) { }
        }
        all = l
    }

    fun find(key: String): FontOpt = all.firstOrNull { it.key == key } ?: all.first()
}
