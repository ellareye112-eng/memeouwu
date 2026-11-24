package com.milauncher.app

val BAR_STYLES = listOf("solid" to "Sólido", "grad" to "Degradado", "diag" to "Diagonal", "glass" to "Cristal")
val BAR_FORMS = listOf("full" to "Completa", "card" to "Tarjeta", "pill" to "Píldora")
val BAR_HEIGHTS = listOf(48 to "Compacta", 57 to "Normal", 68 to "Alta", 80 to "Extra alta")
val GLOWS = listOf("none" to "Ninguno", "always" to "Constante", "breath" to "Respirar", "random" to "Al azar",
    "blink" to "Parpadeo", "wave" to "Ola", "seq" to "Secuencia", "rainbow" to "Arcoíris")
val DIRS = listOf("lr" to "Izq → Der", "rl" to "Der → Izq", "center" to "Desde el centro", "rand" to "Al azar")
val TAPS = listOf("none" to "Ninguno", "destellos" to "Destellos", "estrellas" to "Estrellas", "corazones" to "Corazones",
    "puntos" to "Puntos", "flores" to "Flores", "rombos" to "Rombos", "confeti" to "Confeti",
    "burbujas" to "Burbujas", "onda" to "Onda", "chispas" to "Chispas")
val AMTS = listOf(1 to "Pocos", 2 to "Medio", 3 to "Muchos")
val TEXT_STYLES = listOf("normal" to "Normal", "bold" to "Negrita", "italic" to "Cursiva", "caps" to "MAYÚSCULAS")
val FONT_SCALES = listOf(0.85f to "Pequeño", 1f to "Normal", 1.2f to "Grande", 1.4f to "Enorme")
val ICON_SIZES = listOf(30 to "Pequeño", 38 to "Normal", 46 to "Grande", 54 to "Enorme")
val FRAMES = listOf(0f to "Ninguno", 2f to "Fino", 3.5f to "Medio", 5.5f to "Grueso", 8f to "Extra")
val ICON_GLOWS = listOf(false to "Sin brillo", true to "Con brillo")

val BAR_PALETTE = listOf(0xFF2E9E5B, 0xFF4A7BD1, 0xFFE8832A, 0xFF9B5DE5, 0xFFD92B2B, 0xFF25B85C, 0xFF2D7FF9, 0xFF8A8A8F).map { androidx.compose.ui.graphics.Color(it) }
val PASTEL_PALETTE = listOf(0xFFFFD6A5, 0xFFFFADAD, 0xFFFDFFB6, 0xFFCAFFBF, 0xFF9BF6FF, 0xFFA0C4FF, 0xFFBDB2FF, 0xFFFFC6FF).map { androidx.compose.ui.graphics.Color(it) }
val TEXT_PALETTE = listOf(0xFFFFFFFF, 0xFFFFF3B0, 0xFFFFD6A5, 0xFFFFADAD, 0xFFCAFFBF, 0xFF9BF6FF, 0xFFBDB2FF, 0xFF111111).map { androidx.compose.ui.graphics.Color(it) }
val CONFETTI = listOf(0xFFFF9AA2, 0xFFFFDAC1, 0xFFFDFFB6, 0xFFB5EAD7, 0xFF9BF6FF, 0xFFC7CEEA, 0xFFFFC6FF).map { androidx.compose.ui.graphics.Color(it) }

class Theme(val name: String, val bars: List<Int>, val shapes: List<String>, val font: String, val iconShape: String,
            val frame: Float, val glow: String, val barStyle: String)

val THEMES = listOf(
    Theme("Neón", listOf(0xFFFF2BD6, 0xFF00B8D4, 0xFF76FF03, 0xFFFFD600, 0xFFFF3D00, 0xFF7C4DFF).map { it.toInt() }, listOf("rayos", "destellos", "hexagonos", "estrellas"), "sans-serif-medium", "hexagono", 2f, "always", "grad"),
    Theme("Pastel", listOf(0xFFF48FB1, 0xFF90CAF9, 0xFFA5D6A7, 0xFFFFCC80, 0xFFCE93D8, 0xFF80DEEA).map { it.toInt() }, listOf("nubes", "corazones", "flores", "gotas"), "casual", "circulo", 3.5f, "random", "solid"),
    Theme("Ocaso", listOf(0xFFE8590C, 0xFFD6336C, 0xFF9C36B5, 0xFFF08C00, 0xFFC2255C).map { it.toInt() }, listOf("olas", "arcoiris", "sol", "ondas"), "cursive", "redondeado", 0f, "breath", "diag"),
    Theme("Bosque", listOf(0xFF2F9E44, 0xFF5C940D, 0xFF0B7285, 0xFF4D7C0F).map { it.toInt() }, listOf("hojas", "rombos", "puntos", "cruces"), "serif", "flor", 2f, "random", "grad"),
    Theme("Océano", listOf(0xFF1971C2, 0xFF0C8599, 0xFF3B5BDB, 0xFF1098AD).map { it.toInt() }, listOf("ondas", "gotas", "anillos", "olas"), "sans-serif-light", "gota", 0f, "wave", "diag"),
    Theme("Arcade", listOf(0xFFE03131, 0xFFF08C00, 0xFF2F9E44, 0xFF1C7ED6, 0xFFAE3EC9).map { it.toInt() }, listOf("ajedrez", "confeti", "triangulos", "zigzag"), "monospace", "rect", 3.5f, "blink", "solid"),
    Theme("Galaxia", listOf(0xFF3B2A7A, 0xFF1D3B8A, 0xFF5A2A82, 0xFF23306B).map { it.toInt() }, listOf("constelacion", "estrellas", "fugaces", "destellos"), "sans-serif-condensed", "circulo", 2f, "random", "glass"),
    Theme("Cristal", listOf(0xFF74C0FC, 0xFFB197FC, 0xFF63E6BE, 0xFFFFA8A8).map { it.toInt() }, listOf("cristales", "rombos", "picos", "cuadricula"), "sans-serif-thin", "gema", 2f, "seq", "glass")
)
val ENTRANCES = listOf("inL" to "Deslizar", "inZ" to "Zoom", "inD" to "Caer", "inB" to "Rebote", "inF" to "Girar")
val SEQ_STEPS = listOf(1800 to "Lenta", 1200 to "Media", 700 to "Rápida")
