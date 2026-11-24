package com.milauncher.app

import org.json.JSONObject

/** Todo lo personalizable de una barra de app. null en un color = automático. */
data class AppStyle(
    val barColor: Int? = null,
    val barStyle: String = "solid",
    val barForm: String = "full",
    val barHeight: Int = 57,
    val deco: String = "none",
    val decoColor: Int? = null,
    val glow: String = "none",
    val order: Int? = null,
    val dir: String = "lr",
    val tap: String = "destellos",
    val amt: Int = 2,
    val font: String = "def",
    val fontScale: Float = 1f,
    val textColor: Int? = null,
    val textStyle: String = "normal",
    val iconShape: String = "redondeado",
    val iconBg: Int? = null,
    val hasImage: Boolean = false,
    val frame: Float = 0f,
    val frameColor: Int? = null,
    val iconSize: Int = 38,
    val iconGlow: Boolean = false,
    val sound: String = "clic",
    val volume: Float = 0.6f,
    val soundUri: String? = null,
    val soundName: String? = null
)

private fun JSONObject.intOrNull(k: String): Int? = if (has(k) && !isNull(k)) getInt(k) else null
private fun JSONObject.strOrNull(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null

fun AppStyle.toJson(): String = JSONObject().apply {
    put("barColor", barColor ?: JSONObject.NULL)
    put("barStyle", barStyle); put("barForm", barForm); put("barHeight", barHeight)
    put("deco", deco); put("decoColor", decoColor ?: JSONObject.NULL)
    put("glow", glow); put("order", order ?: JSONObject.NULL); put("dir", dir)
    put("tap", tap); put("amt", amt)
    put("font", font); put("fontScale", fontScale.toDouble())
    put("textColor", textColor ?: JSONObject.NULL); put("textStyle", textStyle)
    put("iconShape", iconShape); put("iconBg", iconBg ?: JSONObject.NULL)
    put("hasImage", hasImage); put("frame", frame.toDouble())
    put("frameColor", frameColor ?: JSONObject.NULL)
    put("iconSize", iconSize); put("iconGlow", iconGlow)
    put("sound", sound); put("volume", volume.toDouble())
    put("soundUri", soundUri ?: JSONObject.NULL); put("soundName", soundName ?: JSONObject.NULL)
}.toString()

fun styleFromJson(s: String): AppStyle {
    val d = AppStyle()
    return try {
        val o = JSONObject(s)
        AppStyle(
            barColor = o.intOrNull("barColor"),
            barStyle = o.optString("barStyle", d.barStyle),
            barForm = o.optString("barForm", d.barForm),
            barHeight = o.optInt("barHeight", d.barHeight),
            deco = o.optString("deco", d.deco),
            decoColor = o.intOrNull("decoColor"),
            glow = o.optString("glow", d.glow),
            order = o.intOrNull("order"),
            dir = o.optString("dir", d.dir),
            tap = o.optString("tap", d.tap),
            amt = o.optInt("amt", d.amt),
            font = o.optString("font", d.font),
            fontScale = o.optDouble("fontScale", d.fontScale.toDouble()).toFloat(),
            textColor = o.intOrNull("textColor"),
            textStyle = o.optString("textStyle", d.textStyle),
            iconShape = o.optString("iconShape", d.iconShape),
            iconBg = o.intOrNull("iconBg"),
            hasImage = o.optBoolean("hasImage", false),
            frame = o.optDouble("frame", 0.0).toFloat(),
            frameColor = o.intOrNull("frameColor"),
            iconSize = o.optInt("iconSize", d.iconSize),
            iconGlow = o.optBoolean("iconGlow", false),
            sound = o.optString("sound", d.sound),
            volume = o.optDouble("volume", d.volume.toDouble()).toFloat(),
            soundUri = o.strOrNull("soundUri"),
            soundName = o.strOrNull("soundName")
        )
    } catch (e: Exception) { d }
}

/** Estilo inicial: algunas apps traen una decoración de muestra. */
fun defaultFor(label: String): AppStyle = when (label) {
    "ShareMe" -> AppStyle(deco = "lineas", glow = "seq", order = 3)
    "Teams" -> AppStyle(deco = "barras", glow = "random")
    "Teléfono", "Phone" -> AppStyle(deco = "estrellas", glow = "always")
    "Temas", "Themes" -> AppStyle(deco = "diagonales", glow = "seq", order = 1)
    "Temu" -> AppStyle(deco = "flechas", glow = "seq", order = 2)
    else -> AppStyle()
}

/** Copia solo el "look" (no colores de barra ni imágenes) a otra barra. */
fun AppStyle.withLookOf(o: AppStyle) = copy(
    deco = o.deco, decoColor = o.decoColor, glow = o.glow, dir = o.dir, tap = o.tap, amt = o.amt,
    font = o.font, fontScale = o.fontScale, textColor = o.textColor, textStyle = o.textStyle,
    iconShape = o.iconShape, frame = o.frame, frameColor = o.frameColor, iconSize = o.iconSize,
    iconGlow = o.iconGlow, sound = o.sound, volume = o.volume,
    barStyle = o.barStyle, barForm = o.barForm, barHeight = o.barHeight
)
