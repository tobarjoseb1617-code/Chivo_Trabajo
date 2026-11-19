# ChivoTrabajo

## Propósito

ChivoTrabajo es una aplicación Android que conecta a clientes con profesionales de oficios en El Salvador. Cubre el proceso desde la publicación de una solicitud de servicio hasta su seguimiento y calificación.

## Funciones principales

- **Clientes:** publican solicitudes con descripción, presupuesto, fotos y ubicación en el mapa. Pueden revisar propuestas, aceptar una y consultar el historial de sus servicios.
- **Profesionales:** consultan solicitudes cercanas, envían cotizaciones y muestran trabajos previos en su perfil y portafolio.
- **Gestión del servicio:** cliente y profesional pueden comunicarse por chat, consultar el estado del trabajo y validar el inicio con un PIN. El cliente puede calificar el servicio al finalizar.
- **Cuentas e identidad:** registro por rol, inicio de sesión con correo y contraseña o Google, y captura del DUI con reconocimiento de texto.
- **Administración:** panel para consultar estadísticas y detalles de las solicitudes.

## Tecnologías usadas

| Área | Tecnologías |
| --- | --- |
| Aplicación Android | Kotlin 2.0.21, AndroidX, interfaces XML, View Binding y Material Components |
| Arquitectura | ViewModels, LiveData, corrutinas y repositorios de datos |
| Autenticación y datos | Firebase Authentication, Cloud Firestore, Firebase Storage y AndroidX Credential Manager para Google |
| Imágenes | Cloudinary para cargas y Glide para visualización |
| Mapas | Leaflet y OpenStreetMap dentro de un WebView |
| Cámara y OCR | CameraX y ML Kit Text Recognition |
| Compilación | Gradle Wrapper 8.9 y Android Gradle Plugin 8.7.3 |

## Requisitos e instalación

- Android Studio compatible con Android Gradle Plugin 8.7.3.
- JDK 21 para ejecutar el daemon de Gradle, según la configuración del repositorio. El código de la app se compila con destino JVM 11.
- Android SDK Platform 35. La aplicación requiere Android 7.0 (API 24) o posterior.
- Un dispositivo físico o emulador. Para probar el acceso con Google en un emulador, usa una imagen con Google Play. Para probar la captura del DUI, usa un dispositivo con cámara.
- Conexión a Internet y acceso a proyectos propios o de desarrollo en Firebase y Cloudinary.

Clona el repositorio, abre su carpeta raíz en Android Studio e instala Android SDK Platform 35 desde **SDK Manager**. Selecciona JDK 21 en la configuración de Gradle de Android Studio. El Wrapper descarga la versión de Gradle que usa el proyecto.

## Configuración local

### Firebase

1. Registra una app Android en Firebase con el identificador `com.cherodevscode.chivo_trabajo`.
2. Habilita los proveedores **Correo electrónico/contraseña** y **Google** en Firebase Authentication. Para Google, registra las huellas del certificado con el que firmas la app. Puedes consultarlas con `gradlew.bat :app:signingReport` en Windows o `./gradlew :app:signingReport` en macOS y Linux.
3. Descarga el archivo `google-services.json` de tu proyecto después de configurar Google y guárdalo como `app/google-services.json`.
4. Configura Cloud Firestore y Firebase Storage con las reglas de acceso de tu entorno. Los flujos de la app dependen del acceso autorizado a esos datos.

### Android SDK y Cloudinary

Conserva el valor de `sdk.dir` que Android Studio genera en `local.properties`. Agrega las dos propiedades de Cloudinary con los valores de tu entorno. El preset debe admitir cargas sin firma, que es el mecanismo que usa la app.

```properties
CLOUDINARY_CLOUD_NAME=tu_nombre_de_nube
CLOUDINARY_UPLOAD_PRESET=tu_preset_de_desarrollo
```

`local.properties` y `app/google-services.json` están excluidos por Git. Mantén las claves privadas y contraseñas fuera del repositorio y de la app. Los dos valores de Cloudinary del ejemplo se incorporan al APK mediante `BuildConfig`.

## Ejecución

En Android Studio, sincroniza Gradle, selecciona la configuración **app**, elige un dispositivo y pulsa **Run**.

También puedes compilar e instalar la variante de depuración desde la raíz. Para instalarla, conecta un dispositivo con depuración USB habilitada o inicia un emulador.

**Windows (PowerShell)**

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

**macOS o Linux**

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

El APK generado queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Documentación adicional

- [Arquitectura del proyecto](ARCHITECTURE.md)
- [Funcionalidades y navegación](FUNCIONALIDADES_Y_NAVEGACION.md)
- [Diseño de la interfaz](DISENO_MATERIAL3.md)