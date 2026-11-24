package com.milauncher.app

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun launchApp(ctx: Context, view: android.view.View, pkg: String) {
    val i = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        val o = ActivityOptions.makeScaleUpAnimation(view, view.width / 2, view.height / 2, 1, 1)
        ctx.startActivity(i, o.toBundle())
    } catch (e: Exception) {
        try { ctx.startActivity(i) } catch (e2: Exception) { }
    }
}

@Composable
fun AppIcon(app: AppEntry, st: AppStyle, custom: ImageBitmap?, glowColor: Color) {
    val shape = remember(st.iconShape) { IconShapes.shape(st.iconShape) }
    val frameCol = st.frameColor?.let { Color(it) } ?: Color.White
    val iconBg = st.iconBg
    Box(
        Modifier.size(st.iconSize.dp).then(
            if (st.iconGlow) Modifier.drawBehind {
                drawCircle(Brush.radialGradient(listOf(glowColor.copy(alpha = .55f), Color.Transparent)), radius = size.minDimension * .85f)
            } else Modifier
        )
    ) {
        Box(Modifier.matchParentSize().clip(shape).then(if (st.frame > 0f) Modifier.background(frameCol) else Modifier)) {
            Box(
                Modifier.fillMaxSize().padding(st.frame.dp).clip(shape)
                    .then(if (iconBg != null) Modifier.background(Color(iconBg)) else Modifier)
            ) {
                Image(bitmap = custom ?: app.icon, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AppRow(app: AppEntry, idx: Int, st: AppStyle, g: LauncherState, onEdit: () -> Unit) {
    val ctx = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val stNow by rememberUpdatedState(st)
    val editNow by rememberUpdatedState(onEdit)

    val full = st.barForm == "full"
    val base = st.barColor?.let { Color(it) } ?: app.base
    val k = if (st.barColor != null) 0.6f else 0.4f
    val decoCol = st.decoColor?.let { Color(it) } ?: pastel(base)
    val els = remember(st.deco) { Deco.get(st.deco) }
    val shape = when (st.barForm) { "pill" -> RoundedCornerShape(40.dp); "card" -> RoundedCornerShape(12.dp); else -> RectangleShape }
    val brush: Brush = remember(base, k, st.barStyle) {
        when (st.barStyle) {
            "grad" -> Brush.horizontalGradient(listOf(mix(base, k + .15f), mix(base, k - .18f)))
            "diag" -> Brush.linearGradient(listOf(mix(base, k + .2f), mix(base, k - .2f)))
            "glass" -> SolidColor(base.copy(alpha = .34f))
            else -> SolidColor(mix(base, k))
        }
    }

    val rg = remember(app.pkg) { RowGlow(app.pkg) }
    SideEffect {
        rg.idx = idx; rg.mode = if (st.deco == "none") "none" else st.glow; rg.order = st.order; rg.dir = st.dir; rg.els = els
        if (rg.flash.size != els.size) rg.flash = LongArray(els.size) { -100000L }
    }
    DisposableEffect(app.pkg) { Registry.rows[app.pkg] = rg; onDispose { Registry.rows.remove(app.pkg) } }

    val parts = remember { mutableStateListOf<Particle>() }
    val needClock = g.glowOn && st.deco != "none" && st.glow != "none"

    // Animación de entrada (solo la primera vez que aparece, o al pulsar "Repetir")
    val first = remember(g.replayKey) { g.played.add(app.pkg + "#" + g.replayKey) }
    val enter = remember(g.replayKey) { Animatable(if (first) 0f else 1f) }
    LaunchedEffect(g.replayKey) {
        if (first) {
            delay(minOf(idx, 12) * 45L)
            if (g.entrance == "inB") enter.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 300f)) else enter.animateTo(1f, tween(500))
        }
    }

    val custom = remember(st.hasImage, g.iconVer[app.pkg]) { if (st.hasImage) g.store.loadIcon(app.pkg) else null }
    val fo = remember(st.font, g.fontVer) { Fonts.find(st.font) }

    Box(
        Modifier.fillMaxWidth()
            .padding(horizontal = if (full) 0.dp else 8.dp, vertical = if (full) 0.dp else 3.dp)
            .graphicsLayer {
                val e = enter.value
                when (g.entrance) {
                    "inZ" -> { alpha = e.coerceIn(0f, 1f); scaleX = .7f + .3f * e; scaleY = .7f + .3f * e }
                    "inD" -> { alpha = e.coerceIn(0f, 1f); translationY = -18f * this.density * (1f - e) }
                    "inB" -> { alpha = (e * 2.5f).coerceIn(0f, 1f); translationX = -40f * this.density * (1f - e) }
                    "inF" -> { alpha = e.coerceIn(0f, 1f); rotationX = 70f * (1f - e); cameraDistance = 12f * this.density }
                    else -> { alpha = e.coerceIn(0f, 1f); translationX = -14f * this.density * (1f - e) }
                }
            }
            .clip(shape)
            .background(brush)
            .then(if (st.barStyle == "glass") Modifier.border(1.dp, Color(0x4DFFFFFF), shape) else Modifier)
            .heightIn(min = st.barHeight.dp)
            .pointerInput(app.pkg) {
                detectTapGestures(
                    onLongPress = { editNow() },
                    onTap = { off ->
                        val s = stNow
                        if (g.soundOn) Sfx.play(ctx, s)
                        spawnParticles(parts, s, off, app.base, density)
                        scope.launch { delay(if (s.tap == "none") 0L else 320L); launchApp(ctx, view, app.pkg) }
                    }
                )
            }
            .drawBehind {
                val now = if (needClock || parts.size > 0) Anim.time.longValue else 0L
                if (st.deco != "none") drawDeco(rg, els, st.glow, g.glowOn, decoCol, now)
                if (full) drawLine(Color(0x24FFFFFF), Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), 1f)
                if (needClock) { val pa = pulseAlpha(now, rg); if (pa > 0f) drawRect(Color.White.copy(alpha = pa)) }
            }
            .drawWithContent {
                drawContent()
                if (parts.size > 0) drawParticles(parts, Anim.time.longValue)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = if (st.barForm == "pill") 24.dp else 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (st.textStyle == "caps") app.label.uppercase() else app.label,
                color = st.textColor?.let { Color(it) } ?: Color.White,
                fontFamily = fo.family,
                fontSize = (16f * st.fontScale).sp,
                fontWeight = if (st.textStyle == "bold") FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (st.textStyle == "italic") FontStyle.Italic else FontStyle.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(Color(0xA6000000), Offset(0f, 2f), 6f)),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            AppIcon(app, st, custom, pastel(base))
        }
    }
}

@Composable
fun LauncherApp(g: LauncherState) {
    val ctx = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<AppEntry?>(null) }
    var showGlobal by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(reload) { apps = loadApps(ctx) }

    DisposableEffect(Unit) {
        val rc = object : BroadcastReceiver() { override fun onReceive(c: Context?, i: Intent?) { reload++ } }
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED); addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(ctx, rc, f, ContextCompat.RECEIVER_EXPORTED)
        onDispose { try { ctx.unregisterReceiver(rc) } catch (e: Exception) { } }
    }

    // Reloj de frames bajo demanda
    LaunchedEffect(Unit) {
        while (true) {
            val cont = g.active && g.glowOn && Registry.rows.values.any { it.continuous && it.els.isNotEmpty() }
            if (g.active && (cont || Anim.now() < Anim.until)) withFrameMillis { Anim.time.longValue = Anim.now() } else delay(60)
        }
    }
    // Brillo "al azar"
    LaunchedEffect(Unit) {
        while (true) {
            delay(420)
            if (g.active && g.glowOn) for (rg in Registry.rows.values) {
                if (rg.mode == "random" && rg.els.isNotEmpty() && rg.flash.size == rg.els.size) {
                    val now = Anim.now()
                    if (Random.nextFloat() < .6f) rg.flash[Random.nextInt(rg.els.size)] = now
                    if (Random.nextFloat() < .3f) rg.flash[Random.nextInt(rg.els.size)] = now
                    Anim.need(1300L)
                }
            }
        }
    }
    // Brillo "ola"
    LaunchedEffect(Unit) {
        while (true) {
            delay(2200)
            if (g.active && g.glowOn) for (rg in Registry.rows.values) if (rg.mode == "wave") sweep(rg, 1300f, false)
        }
    }
    // Brillo en "secuencia" (cada barra en su turno, en ciclo)
    LaunchedEffect(Unit) {
        var i = 0
        while (true) {
            delay(g.seqStep.toLong())
            if (g.active && g.glowOn) {
                val l = Registry.rows.values.filter { it.mode == "seq" && it.els.isNotEmpty() }.sortedBy { it.order ?: (100 + it.idx) }
                if (l.isNotEmpty()) { sweep(l[i % l.size], g.seqStep * .55f, true); i++ }
            }
        }
    }
    LaunchedEffect(g.homeTick) {
        if (g.homeTick > 0) { editing = null; showGlobal = false; query = ""; listState.animateScrollToItem(0) }
    }
    BackHandler(enabled = editing != null || showGlobal || query.isNotEmpty()) {
        if (editing != null) editing = null else if (showGlobal) showGlobal = false else query = ""
    }

    val filtered = remember(apps, query) { apps.filter { it.label.contains(query, ignoreCase = true) } }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(Modifier.fillMaxSize().background(Color(0xFF141110))) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color(0x1FFFFFFF)).padding(horizontal = 14.dp, vertical = 9.dp),
                        decorationBox = { inner ->
                            if (query.isEmpty()) Text("Buscar app", color = Color(0x8CFFFFFF), fontSize = 14.sp)
                            inner()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(19.dp)).background(Color(0x1FFFFFFF)).clickable { showGlobal = true }, contentAlignment = Alignment.Center) {
                        Text("⚙", color = Color.White, fontSize = 18.sp)
                    }
                }
                LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
                    itemsIndexed(filtered, key = { _, a -> a.pkg }) { i, a ->
                        AppRow(a, i, g.style(a), g) { editing = a }
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
            editing?.let { a -> EditorSheet(a, apps, g) { editing = null } }
            if (showGlobal) GlobalSheet(g, apps) { showGlobal = false }
        }
    }
}
