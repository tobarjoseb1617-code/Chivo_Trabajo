package com.cherodevscode.chivo_trabajo

import android.app.Application
import com.cherodevscode.chivo_trabajo.utils.ThemePreferences

/** Restaura el modo claro u oscuro antes de abrir la primera pantalla. */
class ChivoTrabajoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreferences.applySaved(this)
    }
}
