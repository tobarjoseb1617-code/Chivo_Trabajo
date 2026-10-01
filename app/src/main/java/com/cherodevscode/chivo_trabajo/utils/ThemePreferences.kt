package com.cherodevscode.chivo_trabajo.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Guarda la apariencia en el dispositivo sin modificar los datos de Firebase. */
object ThemePreferences {
    private const val FILE = "apariencia"
    private const val KEY = "modo_oscuro"

    // Mantiene oscuro como primera opción; después respeta lo que eligió la persona.
    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun applySaved(context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark(context)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setDark(context: Context, dark: Boolean) {
        // Guardar antes de cambiar el tema permite recuperar la elección al recrear la pantalla.
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY, dark).apply()
        applySaved(context)
    }
}
