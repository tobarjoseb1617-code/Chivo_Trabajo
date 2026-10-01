# Diseño Material 3 — ChivoTrabajo

Se conserva la preferencia de modo claro u oscuro, guardada en el dispositivo.

## Cambios

- Tarjetas MaterialCardView con borde fino, radios consistentes y sin sombras excesivas.
- Botones MaterialButton: acción principal rellena y acciones secundarias con borde; estados y ripple del tema.
- Campos compatibles con TextInputLayout y TextInputEditText, conservando IDs y validaciones existentes. Los campos de contraseña mantienen su botón de visibilidad.
- Navegación inferior BottomNavigationView en siete pantallas; los IDs originales se encuentran ahora en los menús. Los listeners conservan sus destinos.
- Jerarquía TextAppearance.Material3 para títulos, cuerpo y etiquetas.
- Iconos vectoriales de 24dp en lugar de emojis decorativos; acciones con descripciones de accesibilidad.
- Diálogos MaterialAlertDialogBuilder y controles de portafolio adaptados al tema.

Los cambios están comentados en los layouts, estilos y código Kotlin.

## Validación realizada

Se revisaron XML, referencias de recursos, permanencia de IDs y propiedades utilizadas por ViewBinding. No se pudo compilar ni ejecutar en emulador: el entorno no puede descargar la distribución de Gradle y no tiene Android SDK disponible.

## Verificación en Android Studio

1. Sincronizar Gradle y compilar la app.
2. Recorrer login, registro, inicio, solicitudes, servicios, chat, perfil y portafolio.
3. Cambiar claro/oscuro en Configuración del perfil, cerrar y abrir la app.
4. Revisar pantallas estrechas, texto ampliado, teclado visible y navegación de regreso.

Las pantallas conservan los datos de ejemplo y las funciones que ya tenía el proyecto.
