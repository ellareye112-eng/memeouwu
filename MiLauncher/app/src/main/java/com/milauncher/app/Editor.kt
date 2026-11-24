package com.milauncher.app

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PANEL = Color(0xFF1F1A18)
private val SOFT = Color(0x1FFFFFFF)

@Composable
fun Small(t: String) {
    Text(t, color = Color(0x9EFFFFFF), fontSize = 11.sp, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
}

@Composable
fun TextChip(text: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(14.dp)).background(if (on) Color.White else SOFT)
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = if (on) Color(0xFF222222) else Color.White, fontSize = 12.sp,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> Chips(
    opts: List<Pair<T, String>>, sel: T, scroll: Boolean = false,
    fonts: ((T) -> FontFamily?)? = null, leading: (@Composable (T) -> Unit)? = null, onSel: (T) -> Unit
) {
    val body: @Composable () -> Unit = {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            for ((v, label) in opts) {
                val on = v == sel
                Row(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(if (on) Color.White else SOFT)
                        .clickable { onSel(v) }.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leading?.invoke(v)
                    Text(label, color = if (on) Color(0xFF222222) else Color.White, fontSize = 12.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal, fontFamily = fonts?.invoke(v))
                }
            }
        }
    }
    if (scroll) Box(Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) { body() } else body()
}

@Composable
fun ColorPick(cur: Int?, auto: Color, pal: List<Color>, onPick: (Int?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            TextChip("Auto", cur == null) { onPick(null) }
            for (c in pal) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(c)
                        .border(2.dp, if (cur == c.toArgb()) Color.White else Color(0x59FFFFFF), CircleShape)
                        .clickable { onPick(c.toArgb()) }
                )
            }
            TextChip("Personalizado", open) { open = !open }
        }
        if (open) {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(cur ?: auto.toArgb(), hsv)
            Text("Tono", color = Color(0x9EFFFFFF), fontSize = 11.sp)
            Slider(value = hsv[0], onValueChange = { onPick(android.graphics.Color.HSVToColor(floatArrayOf(it, hsv[1], hsv[2]))) }, valueRange = 0f..360f)
            Text("Saturación", color = Color(0x9EFFFFFF), fontSize = 11.sp)
            Slider(value = hsv[1], onValueChange = { onPick(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], it, hsv[2]))) }, valueRange = 0f..1f)
            Text("Brillo", color = Color(0x9EFFFFFF), fontSize = 11.sp)
            Slider(value = hsv[2], onValueChange = { onPick(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], it))) }, valueRange = 0f..1f)
        }
    }
}

@Composable
private fun DecoPreview(key: String) {
    Canvas(Modifier.size(width = 64.dp, height = 12.dp)) {
        drawDeco(DUMMY_GLOW, Deco.get(key), "none", false, Color.White, 0L, 1f)
    }
    Spacer(Modifier.width(6.dp))
}

@Composable
private fun IconPreview(key: String) {
    Box(Modifier.size(14.dp).clip(IconShapes.shape(key)).background(Color(0xFFCCCCCC)))
    Spacer(Modifier.width(6.dp))
}

@Composable
private fun SheetFrame(onClose: () -> Unit, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().background(Color(0x66000000))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
        )
        AnimatedVisibility(
            visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
            enter = slideInVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth().fillMaxHeight(0.66f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(PANEL)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                    .navigationBarsPadding().padding(horizontal = 14.dp, vertical = 12.dp)
            ) { content() }
        }
    }
}

@Composable
fun EditorSheet(app: AppEntry, apps: List<AppEntry>, g: LauncherState, onClose: () -> Unit) {
    val st = g.style(app)
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    val base = st.barColor?.let { Color(it) } ?: app.base
    fun up(f: (AppStyle) -> AppStyle) = g.update(app, f)

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && g.store.saveIcon(app.pkg, uri)) {
            g.iconVer[app.pkg] = (g.iconVer[app.pkg] ?: 0) + 1
            up { it.copy(hasImage = true) }
        }
    }
    val pickFont = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val k = g.store.importFont(uri)
            if (k != null) { g.fontVer++; up { it.copy(font = k) } }
            else Toast.makeText(ctx, "Elige un archivo .ttf u .otf", Toast.LENGTH_SHORT).show()
        }
    }
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            val nm = g.store.displayName(uri)
            up { it.copy(soundUri = uri.toString(), soundName = nm) }
        }
    }
    val pickRingtone = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        @Suppress("DEPRECATION")
        val uri = r.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            val nm = try { RingtoneManager.getRingtone(ctx, uri)?.getTitle(ctx) ?: "Tono" } catch (e: Exception) { "Tono" }
            up { it.copy(soundUri = uri.toString(), soundName = nm) }
        }
    }

    SheetFrame(onClose) {
        Text("Personalizar ${app.label}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.size(8.dp))
        val tabs = listOf("Barra", "Decoración", "Brillo", "Texto", "Icono", "Sonido")
        Row(Modifier.horizontalScroll(rememberScrollState()).clip(RoundedCornerShape(12.dp)).background(Color(0x14FFFFFF)).padding(3.dp)) {
            tabs.forEachIndexed { i, t ->
                Box(
                    Modifier.clip(RoundedCornerShape(9.dp)).background(if (tab == i) Color.White else Color.Transparent)
                        .clickable { tab = i }.padding(horizontal = 10.dp, vertical = 6.dp)
                ) { Text(t, color = if (tab == i) Color(0xFF222222) else Color.White, fontSize = 12.sp) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (tab) {
                0 -> {
                    Small("Color de la barra")
                    ColorPick(st.barColor, app.base, BAR_PALETTE) { c -> up { it.copy(barColor = c) } }
                    Small("Estilo de fondo"); Chips(BAR_STYLES, st.barStyle) { v -> up { it.copy(barStyle = v) } }
                    Small("Forma de la barra"); Chips(BAR_FORMS, st.barForm) { v -> up { it.copy(barForm = v) } }
                    Small("Altura"); Chips(BAR_HEIGHTS, st.barHeight) { v -> up { it.copy(barHeight = v) } }
                }
                1 -> {
                    Small("Forma (${DECO_NAMES.size - 1} opciones)")
                    Chips(DECO_NAMES, st.deco, scroll = true, leading = { k -> if (k != "none") DecoPreview(k) }) { v -> up { it.copy(deco = v) } }
                    Small("Color de la decoración")
                    ColorPick(st.decoColor, pastel(base), PASTEL_PALETTE) { c -> up { it.copy(decoColor = c) } }
                }
                2 -> {
                    Small("Brillo de la decoración"); Chips(GLOWS, st.glow) { v -> up { it.copy(glow = v) } }
                    Small("Dirección (secuencia y ola)"); Chips(DIRS, st.dir) { v -> up { it.copy(dir = v) } }
                    Small("Turno en el ciclo (secuencia)")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextChip("−", false) { up { it.copy(order = if (it.order == null) null else if (it.order <= 1) null else it.order - 1) } }
                        Text(st.order?.toString() ?: "Auto", color = Color.White, fontSize = 13.sp)
                        TextChip("+", false) { up { it.copy(order = if (it.order == null) 1 else minOf(it.order + 1, apps.size.coerceAtLeast(1))) } }
                    }
                    Small("Destellos al tocar"); Chips(TAPS, st.tap) { v -> up { it.copy(tap = v) } }
                    Small("Cantidad"); Chips(AMTS, st.amt) { v -> up { it.copy(amt = v) } }
                }
                3 -> {
                    Small("Fuente del nombre (${Fonts.all.size} opciones)")
                    Chips(Fonts.all.map { it.key to it.label }, st.font, scroll = true, fonts = { k -> Fonts.find(k).family }) { v -> up { it.copy(font = v) } }
                    Spacer(Modifier.size(8.dp))
                    TextChip("Importar fuente (.ttf / .otf)", false) { pickFont.launch(arrayOf("*/*")) }
                    Small("Tamaño"); Chips(FONT_SCALES, st.fontScale) { v -> up { it.copy(fontScale = v) } }
                    Small("Estilo"); Chips(TEXT_STYLES, st.textStyle) { v -> up { it.copy(textStyle = v) } }
                    Small("Color del texto")
                    ColorPick(st.textColor, Color.White, TEXT_PALETTE) { c -> up { it.copy(textColor = c) } }
                }
                4 -> {
                    Small("Imagen")
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        TextChip("Elegir de la galería", false) { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        TextChip("Quitar imagen", false) { up { it.copy(hasImage = false) } }
                    }
                    Small("Forma del icono (${IconShapes.options.size} opciones)")
                    Chips(IconShapes.options, st.iconShape, scroll = true, leading = { k -> IconPreview(k) }) { v -> up { it.copy(iconShape = v) } }
                    Small("Tamaño"); Chips(ICON_SIZES, st.iconSize) { v -> up { it.copy(iconSize = v) } }
                    Small("Color de fondo del icono")
                    ColorPick(st.iconBg, app.base, BAR_PALETTE) { c -> up { it.copy(iconBg = c) } }
                    Small("Marco"); Chips(FRAMES, st.frame) { v -> up { it.copy(frame = v) } }
                    Small("Color del marco")
                    ColorPick(st.frameColor, Color.White, PASTEL_PALETTE) { c -> up { it.copy(frameColor = c) } }
                    Small("Resplandor"); Chips(ICON_GLOWS, st.iconGlow) { v -> up { it.copy(iconGlow = v) } }
                }
                else -> {
                    Small("Sonido al tocar (${Sfx.options.size} opciones)")
                    Chips(Sfx.options, if (st.soundUri != null) "_" else st.sound, scroll = true) { v ->
                        val n = st.copy(sound = v, soundUri = null, soundName = null)
                        up { it.copy(sound = v, soundUri = null, soundName = null) }
                        Sfx.play(ctx, n)
                    }
                    Small("Audio del dispositivo")
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        TextChip("Elegir archivo", false) { pickAudio.launch(arrayOf("audio/*")) }
                        TextChip("Tonos del sistema", false) {
                            pickRingtone.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            })
                        }
                        TextChip("Quitar", false) { up { it.copy(soundUri = null, soundName = null) } }
                    }
                    if (st.soundUri != null) Text("Usando: ${st.soundName ?: "audio"}", color = Color(0xB3FFFFFF), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                    Small("Volumen")
                    Slider(value = st.volume, onValueChange = { v -> up { it.copy(volume = v) } }, valueRange = 0f..1f)
                    TextChip("Probar sonido", false) { Sfx.play(ctx, st) }
                }
            }
            Spacer(Modifier.size(8.dp))
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TextChip("Restablecer", false) { up { defaultFor(app.label) } }
            TextChip("Copiar estilo a todas", false) { g.copyLook(apps, app) }
            Spacer(Modifier.weight(1f))
            TextChip("Listo", true) { onClose() }
        }
    }
}

@Composable
fun GlobalSheet(g: LauncherState, apps: List<AppEntry>, onClose: () -> Unit) {
    SheetFrame(onClose) {
        Text("Ajustes del launcher", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Sonido al tocar", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(checked = g.soundOn, onCheckedChange = { g.setSound(it) })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Brillo de las decoraciones", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(checked = g.glowOn, onCheckedChange = { g.setGlow(it) })
            }
            Small("Velocidad de la secuencia")
            Chips(SEQ_STEPS, g.seqStep) { v -> g.setStep(v) }
            Small("Temas (se aplican a todas las barras)")
            Chips(THEMES.map { it.name to it.name }, "") { n -> THEMES.firstOrNull { it.name == n }?.let { g.applyTheme(it, apps) } }
            Small("Animación de entrada")
            Chips(ENTRANCES, g.entrance) { v -> g.setEntrance(v) }
            Spacer(Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                TextChip("Repetir animación", false) { g.replayKey++ }
                TextChip("Reiniciar todo", false) { g.resetAll(apps) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            Spacer(Modifier.weight(1f))
            TextChip("Listo", true) { onClose() }
        }
    }
}
