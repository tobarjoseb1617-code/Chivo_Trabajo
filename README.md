# ChivoTrabajo

Aplicación Android nativa desarrollada en Kotlin para conectar clientes con profesionales de oficios en El Salvador. La app está orientada a facilitar la búsqueda, publicación y gestión de servicios de mantenimiento y trabajos especializados, con un flujo de autenticación, geolocalización, contratación, seguimiento y calificación.

## Descripción general

ChivoTrabajo es una plataforma móvil tipo marketplace de servicios domésticos y técnicos. Permite que:

- Un cliente publique una solicitud de servicio con ubicación, presupuesto y fotos.
- Los profesionales cercanos visualicen oportunidades disponibles.
- Los profesionales envíen propuestas o cotizaciones.
- El cliente acepte una oferta y coordine la ejecución del servicio.
- Se valide la identidad del profesional y la entrega con un sistema de PIN de seguridad.
- Se mantenga un historial de servicios, chats y calificaciones.

El proyecto está construido con una estructura modular basada en Android nativo, Firebase y componentes modernos de Jetpack.

## Stack tecnológico

- Lenguaje: Kotlin
- IDE principal: Android Studio
- SDK Android: minSdk 24, targetSdk 35, compileSdk 35
- Arquitectura: MVVM + Repository Pattern
- UI: Activities, ViewBinding, XML layouts, Material Components
- Persistencia y backend: Firebase Auth, Firestore, Firebase Storage
- Autenticación social: Google Sign-In
- Imágenes y contenido visual: Glide, Cloudinary
- Geolocalización: Leaflet.js + OpenStreetMap embebidos en WebView
- Cámara y visión: CameraX, ML Kit Text Recognition
- Async: Kotlin Coroutines
- Navegación: Intents y flujo de actividades por rol

## Arquitectura

El repositorio sigue una separación clara en capas:

- Capa de UI: actividades y pantallas para autenticación, cliente, profesional, chat, perfil, etc.
- Capa de lógica: ViewModels y lógica de estado de la aplicación.
- Capa de datos: modelos y repositorios para autenticación y persistencia en Firestore.
- Servicios externos: Firebase, Cloudinary y mapas.

Esta estructura es coherente con la documentación incluida en los archivos:

- `ARCHITECTURE.md`
- `DOCUMENTACION_PRESENTACION.md`
- `FUNCIONALIDADES_Y_NAVEGACION.md`

## Funcionalidades principales

### 1. Autenticación y registro

- Inicio de sesión con correo y contraseña.
- Registro de usuarios con roles Cliente o Profesional.
- Inicio de sesión con Google.
- Manejo de sesión con Firebase Authentication.
- Ruteo dinámico según el tipo de usuario.

### 2. Registro geolocalizado

- El cliente puede registrar su ubicación exacta usando un mapa interactivo.
- Se integra Leaflet.js + OpenStreetMap dentro de un WebView.
- Se guardan coordenadas (latitud/longitud) para ubicar servicios y profesionales.

### 3. Gestión de servicios

- Los clientes publican solicitudes de trabajo con descripción, fotos, presupuesto y ubicación.
- Los profesionales reciben solicitudes cercanas.
- Los profesionales cotizan o envían propuestas.
- El cliente acepta la mejor propuesta.

### 4. Seguridad y validación

- Generación de PIN de 4 dígitos para la ejecución del servicio.
- Validación por parte del profesional al llegar a la ubicación.
- Control de identidad y verificación de DUI.
- Aseguramiento del flujo de servicio en la etapa de ejecución.

### 5. Perfil, portafolio y reputación

- Perfiles personales con foto y datos básicos.
- Portafolio profesional con fotos y descripciones.
- Subida de imágenes a Cloudinary.
- Historial de trabajos, calificación y reseñas.

### 6. Chat y cierre del servicio

- Mensajería entre cliente y profesional.
- Seguimiento del servicio.
- Finalización del trabajo y calificación final con estrellas.

## Estructura del repositorio

```text
Chivo_Trabajo/
├── .artifacts/
├── .idea/
├── app/
│   ├── .gitignore
│   ├── build.gradle.kts
│   └── src/
│       ├── androidTest/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   ├── java/com/cherodevscode/chivo_trabajo/
│       │   │   ├── ChivoTrabajoApplication.kt
│       │   │   ├── data/
│       │   │   │   ├── model/
│       │   │   │   └── repository/
│       │   │   ├── ui/
│       │   │   │   ├── autenticacion/
│       │   │   │   ├── cliente/
│       │   │   │   ├── profesional/
│       │   │   │   ├── chat_y_evaluacion/
│       │   │   │   └── perfil/
│       │   │   └── utils/
│       │   ├── keepRules/
│       │   └── res/
│       └── test/
├── .gitignore
├── ARCHITECTURE.md
├── build.gradle.kts
├── CORRECCIONES_COMPILACION.md
├── DISENO_MATERIAL3.md
├── DOCUMENTACION_PRESENTACION.md
├── FUNCIONALIDADES_Y_NAVEGACION.md
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

## Paquetes principales

La aplicación se organiza bajo el nombre de paquete:

```text
com.cherodevscode.chivo_trabajo
```

Dentro de este paquete se encuentran módulos clave como:

- `data/model`: modelos de negocio y entidades Firestore.
- `data/repository`: lógica de acceso a datos.
- `ui/autenticacion`: login y registro.
- `ui/cliente`: pantallas del cliente.
- `ui/profesional`: pantallas del profesional.
- `ui/chat_y_evaluacion`: comunicación y cierre de servicios.
- `ui/perfil`: ajustes y perfil.
- `utils`: utilidades auxiliares.

## Modelos de datos relevantes

El proyecto define modelos como:

- `Usuario.kt`
- `Profesional.kt`
- `Solicitud.kt`
- `Servicio.kt`
- `Registro.kt`
- `Chat.kt`
- `Pago.kt`
- `Portafolio.kt`
- `Categoria.kt`
- `Calificacion.kt`

Estos modelos reflejan la estructura de datos en Cloud Firestore y permiten manejar perfiles, solicitudes, propuestas, servicios y portafolio de trabajos.

## Requisitos de instalación

### Software necesario

- Android Studio
- JDK 11
- Android SDK con API 35 o compatible
- Dispositivo físico o emulador Android con API 24+

### Firebase

El proyecto usa Firebase. En un entorno real, se requiere:

- `google-services.json` configurado para el proyecto Android.
- Firebase Authentication habilitado.
- Cloud Firestore configurado.
- Firebase Storage habilitado.

### Cloudinary

La app configura valores de Cloudinary desde `local.properties` o usa valores por defecto:

```properties
CLOUDINARY_CLOUD_NAME=djwvfjt7k
CLOUDINARY_UPLOAD_PRESET=chivo_trabajo
```

Esto se refleja en `app/build.gradle.kts`, donde se generan campos `BuildConfig` para la app.

## Configuración local

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Crea o actualiza `local.properties` en la raíz del proyecto.
4. Verifica la presencia de `google-services.json` en la carpeta `app/`.
5. Sincroniza Gradle.

Ejemplo de `local.properties`:

```properties
sdk.dir=/ruta/a/Android/Sdk
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_UPLOAD_PRESET=tu_upload_preset
```

## Cómo ejecutar

```bash
./gradlew assembleDebug
```

o desde Android Studio:

1. Abre el proyecto.
2. Selecciona un dispositivo/emulador.
3. Ejecuta la configuración de la app.

## Flujo típico de uso

1. El usuario inicia sesión o crea una cuenta.
2. Selecciona el rol: Cliente o Profesional.
3. Completa registro con ubicación y datos requeridos.
4. Cliente publica una solicitud.
5. Profesional cercano revisa y responde con propuesta.
6. Cliente acepta y se inicia el servicio.
7. Se valida acceso mediante PIN.
8. Se finaliza con chat y calificación.

## Documentación del proyecto

El repositorio incluye documentación técnica que complementa este README:

- `ARCHITECTURE.md`: arquitectura del sistema y flujo de ejecución.
- `FUNCIONALIDADES_Y_NAVEGACION.md`: funcionalidades y conexión entre clases.
- `DOCUMENTACION_PRESENTACION.md`: guía para presentación del proyecto.
- `CORRECCIONES_COMPILACION.md`: detalles de ajustes de compilación.
- `DISENO_MATERIAL3.md`: diseño y Material 3.

## Observaciones del código

El proyecto está en una etapa funcional con enfoque de aplicación de servicios a nivel local. Los archivos de documentación indican que el sistema está pensado para un entorno real con Firebase y servicios externos, y que cuenta con una estructura sólida para escalar funcionalidades adicionales.

## Licencia

No se especifica licencia explícita en el repositorio dentro de la estructura principal. Si este proyecto se va a reutilizar o distribuir, conviene confirmar la licencia antes de hacerlo.

## Conclusión

ChivoTrabajo es una app Android de tipo marketplace para servicios de oficios, con un diseño orientado a clientes y profesionales, y una base tecnológica sólida en Kotlin, Firebase y servicios cloud. Su organización modular y la documentación incluida facilitan tanto su mantenimiento como su evolución.

---

Este README fue generado a partir del código y la documentación presente en el repositorio para describir de forma precisa la estructura y funcionalidad del proyecto.
