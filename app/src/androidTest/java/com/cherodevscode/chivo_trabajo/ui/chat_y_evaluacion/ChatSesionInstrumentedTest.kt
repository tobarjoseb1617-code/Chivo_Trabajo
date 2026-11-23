package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cherodevscode.chivo_trabajo.data.repository.ChatRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatSesionInstrumentedTest {
    // Preferencias separadas: las pruebas no cambian la sesión del usuario del teléfono.
    private lateinit var contexto: Context

    @Before fun preparar() {
        contexto = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("test_chat_$name", mode)
        }
        limpiar()
    }

    @After fun limpiar() {
        contexto.getSharedPreferences("chivo_credenciales_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun usaLaSesionDelLoginLocalParaElChat() {
        contexto.getSharedPreferences("chivo_credenciales_prefs", Context.MODE_PRIVATE).edit()
            .putString("USER_UID", "cliente-local-prueba").commit()
        assertEquals("cliente-local-prueba", ChatRepository(contexto).usuarioActualUid())
    }

    @Test fun vuelveAConsultarLaSesionDespuesDeCerrarSesion() {
        val preferencias = contexto.getSharedPreferences("chivo_credenciales_prefs", Context.MODE_PRIVATE)
        preferencias.edit().putString("USER_UID", "profesional-local-prueba").commit()
        val repository = ChatRepository(contexto)
        assertEquals("profesional-local-prueba", repository.usuarioActualUid())
        preferencias.edit().clear().commit()
        assertNull(repository.usuarioActualUid())
    }
}
