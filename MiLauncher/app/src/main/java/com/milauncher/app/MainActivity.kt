package com.milauncher.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private lateinit var store: Store
    private lateinit var state: LauncherState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Fonts.load(this)
        store = Store(applicationContext)
        state = LauncherState(store)
        setContent { LauncherApp(state) }
    }

    override fun onResume() { super.onResume(); state.active = true }

    override fun onPause() {
        super.onPause()
        state.active = false
        store.flushNow()
    }

    /** Al pulsar el botón Inicio estando dentro del launcher: vuelve arriba y cierra paneles. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) state.homeTick++
    }
}
