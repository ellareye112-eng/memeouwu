package com.milauncher.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppEntry(val label: String, val pkg: String, val icon: ImageBitmap, val base: Color)

/** Color predominante del icono: es el color automático de la barra. */
fun dominantColor(bmp: Bitmap): Color {
    val s = Bitmap.createScaledBitmap(bmp, 16, 16, true)
    var r = 0f; var g = 0f; var b = 0f; var w = 0f
    val hsv = FloatArray(3)
    for (y in 0 until 16) for (x in 0 until 16) {
        val p = s.getPixel(x, y)
        val a = (p ushr 24) and 0xFF
        if (a < 128) continue
        android.graphics.Color.colorToHSV(p, hsv)
        val wt = hsv[1] * hsv[2] + 0.05f
        r += android.graphics.Color.red(p) * wt; g += android.graphics.Color.green(p) * wt; b += android.graphics.Color.blue(p) * wt; w += wt
    }
    if (w == 0f) return Color(0xFF5B5FC7)
    return Color(r / w / 255f, g / w / 255f, b / w / 255f)
}

@Suppress("DEPRECATION")
suspend fun loadApps(ctx: Context): List<AppEntry> = withContext(Dispatchers.Default) {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { ri ->
            val bmp = ri.loadIcon(pm).toBitmap(96, 96)
            AppEntry(ri.loadLabel(pm).toString(), ri.activityInfo.packageName, bmp.asImageBitmap(), dominantColor(bmp))
        }
        .sortedBy { it.label.lowercase() }
}
