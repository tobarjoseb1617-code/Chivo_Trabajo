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

### Crear y verificar documentos en Cloud Firestore

1. En la consola de Firebase, abre **Firestore Database**. Si aún no existe una base de datos, créala con el ID `(default)` y selecciona su ubicación.
2. Ejecuta la app con tu proyecto de desarrollo. Registra un cliente y un profesional de prueba, completa sus perfiles, publica una solicitud como cliente y envía una propuesta como profesional. Para generar un documento de portafolio, agrega una foto desde el perfil profesional.
3. Abre la pestaña **Datos** en Firestore y comprueba las rutas de la tabla. Firestore crea cada colección con la primera escritura de la app. [Guía de inicio de Firestore](https://firebase.google.com/docs/firestore/quickstart)

| Ruta en Firestore | Cuándo se crea y qué revisar |
| --- | --- |
| `credenciales/{uid}` | Al registrar una cuenta mediante el flujo de autenticación de la app. El ID coincide con el `uid` que usa el perfil. |
| `Usuario/{uid}` | Al guardar el perfil. Revisa `uid` y `tipoUsuario` (`CLIENTE` o `PROFESIONAL`). |
| `profesionales/{uid}` | Al completar el registro profesional. Usa el mismo ID que `Usuario/{uid}`. |
| `profesionales/{uid}/Portafolio/{idFoto}` | Al agregar una foto al portafolio. `idFoto` coincide con el ID generado por Firestore. |
| `Solicitudes/{idSolicitud}` | Al publicar una solicitud. `clientId` contiene el UID del cliente y el estado inicial es `PENDIENTE`. |
| `Propuestas/{idPropuesta}` | Al enviar una propuesta. `solicitudId`, `clienteId` y `profesionalId` enlazan la solicitud y los perfiles. |

#### Campos por colección

En la consola, usa **string** para cadenas, **number** para enteros o dobles, **boolean** para booleanos, **array** para arreglos, **timestamp** para marcas de tiempo y **null** para valores nulos. Los UID e identificadores que enlazan documentos se guardan como cadenas.

##### `credenciales/{uid}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `uid` | Cadena | Identificador de cuenta generado por la app. Coincide con el ID del documento y enlaza con Usuario. |
| `correo` | Cadena | Correo que el inicio de sesión usa para buscar la cuenta. |
| `contrasena` | Cadena | Campo que el flujo actual consulta al iniciar sesión con correo. El flujo de Google guarda un marcador interno. Usa el registro de la app con cuentas de prueba. |

##### `Usuario/{uid}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `uid` | Cadena | Identificador de la cuenta y del documento. |
| `nombre` | Cadena | Nombre que se muestra en el perfil y las propuestas. |
| `apellido` | Cadena | Apellido que acompaña al nombre del usuario. |
| `correo` | Cadena | Correo asociado al perfil. |
| `telefono` | Cadena | Número de contacto guardado como texto. |
| `tipoUsuario` | Cadena | Rol que determina la pantalla al iniciar sesión: CLIENTE, PROFESIONAL o ADMIN. |
| `estadoVerificacion` | Cadena | Estado de verificación del DUI. El registro parte de PENDIENTE. |
| `fotoPerfil` | Cadena | URL de la foto de perfil. |
| `dui` | Cadena | Número del documento de identidad, almacenado como texto. |
| `duiFrenteUrl` | Cadena | URL de la imagen frontal del DUI. |
| `duiDorsoUrl` | Cadena | URL de la imagen posterior del DUI. |
| `direccion` | Cadena | Dirección guardada en el perfil. |
| `ciudad` | Cadena | Ciudad o municipio del usuario. |
| `latitud` | Doble | Latitud de la ubicación del perfil. |
| `longitud` | Doble | Longitud de la ubicación del perfil. |
| `fechaRegistro` | Entero | Fecha del registro en milisegundos desde Unix. |
| `activo` | Booleano | Indicador de cuenta activa. Su valor inicial es true. |

##### `profesionales/{uid}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `uid` | Cadena | Identificador del profesional. Coincide con el ID de su perfil en Usuario. |
| `aniosExperiencia` | Entero | Años de experiencia declarados. |
| `calificacionPromedio` | Doble | Promedio de las calificaciones del profesional. |
| `cantidadCalificaciones` | Entero | Cantidad de calificaciones recibidas. |
| `descripcion` | Cadena | Presentación editable del profesional. |
| `disponible` | Booleano | Indicador de disponibilidad laboral. |
| `especialidad` | Cadena | Especialidad principal que conserva el modelo por compatibilidad. |
| `especialidades` | Arreglo de cadenas | Especialidades seleccionadas durante el registro. |
| `latitud` | Doble | Latitud de la ubicación profesional. |
| `longitud` | Doble | Longitud de la ubicación profesional. |
| `serviciosOfrecidos` | Arreglo de cadenas | Servicios específicos que ofrece el profesional. |
| `verificado` | Booleano | Indicador de verificación del perfil profesional. |
| `zonaTrabajo` | Arreglo de cadenas | Zonas en las que ofrece sus servicios. |

##### `profesionales/{uid}/Portafolio/{idFoto}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `idFoto` | Cadena | ID del documento generado al agregar el elemento al portafolio. |
| `url` | Cadena | URL de la imagen del trabajo o acreditación. |
| `titulo` | Cadena | Título que se muestra en el portafolio. |
| `descripcion` | Cadena | Texto editable sobre el trabajo o la acreditación. |
| `fecha` | Marca de tiempo | Fecha de creación del elemento. |

##### `Solicitudes/{idSolicitud}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `idSolicitud` | Cadena | ID automático del documento, que identifica la solicitud en el historial y en las propuestas. |
| `clientId` | Cadena | UID del cliente que publicó la solicitud. Se usa para filtrar su historial y comprobar quién puede editarla. |
| `categoryId` | Cadena | Valor de la categoría seleccionada en el formulario de la app. |
| `descripcion` | Cadena | Detalle del servicio solicitado. |
| `fotoProblema` | Cadena | URL de la foto principal, conservada por compatibilidad. |
| `fotosProblema` | Arreglo de cadenas | URLs de las fotos adjuntas a la solicitud. |
| `direccion` | Cadena | Dirección donde debe realizarse el servicio. |
| `ciudad` | Cadena | Ciudad o municipio del servicio. |
| `latitud` | Doble | Latitud del punto elegido en el mapa. |
| `longitud` | Doble | Longitud del punto elegido en el mapa. |
| `fechaServicio` | Cadena | Fecha elegida por el cliente en el formulario. |
| `horaServicio` | Cadena | Hora elegida por el cliente en el formulario. |
| `presupuesto` | Cadena | Monto de presupuesto escrito por el cliente. |
| `estado` | Cadena | Estado de la solicitud. Comienza en PENDIENTE y la app escribe ASIGNADA o CANCELADA al asignarla o cancelarla. |
| `profesionalId` | Cadena o nulo | UID del profesional asignado. Puede ser nulo mientras la solicitud está pendiente. |
| `fechaCreacion` | Marca de tiempo | Fecha de publicación que permite ordenar las solicitudes. |
| `fechaActualizacion` | Marca de tiempo | Fecha del último cambio guardado en la solicitud. |

##### `Propuestas/{idPropuesta}`

| Campo | Tipo en Firestore | Uso |
| --- | --- | --- |
| `idPropuesta` | Cadena | ID del documento formado por `${solicitudId}_${profesionalId}`. Permite una propuesta por profesional y solicitud. |
| `solicitudId` | Cadena | ID del documento en Solicitudes al que responde la propuesta. |
| `profesionalId` | Cadena | UID del profesional que envía la propuesta. |
| `clienteId` | Cadena | UID del cliente destinatario. Coincide con clientId en la solicitud. |
| `tipoPropuesta` | Cadena | ACEPTAR_PRESUPUESTO si el profesional acepta el presupuesto o NUEVO_PRECIO si ofrece otro monto. |
| `precioPropuesto` | Doble | Importe de la propuesta. La app exige un número mayor que cero. |
| `mensaje` | Cadena | Texto del profesional para el cliente. |
| `disponibilidad` | Cadena | Fecha, horario o disponibilidad ofrecida como texto. |
| `estado` | Cadena | Estado de la propuesta: PENDIENTE, ACEPTADA o RECHAZADA. |
| `fechaCreacion` | Marca de tiempo | Fecha de envío registrada por el servidor. |
#### Crear un documento manualmente para pruebas

1. En **Datos**, elige **Agregar colección** si la colección aún no existe, o abre la colección y elige **Agregar documento**. Para `Portafolio`, abre primero el documento del profesional y agrega la subcolección desde allí.
2. Escribe el nombre de la colección tal como aparece en la tabla, respetando mayúsculas y minúsculas. En los perfiles usa el UID generado por la app como ID de documento. Para una solicitud o foto de prueba puedes usar **ID automático** y copiarlo en el campo `idSolicitud` o `idFoto`, respectivamente. Para las propuestas, usa el flujo de la app para conservar las referencias y la lógica de creación.
3. Agrega los campos con sus tipos y pulsa **Guardar**. Como ejemplo, para una solicitud de prueba usa `clientId` (cadena con el UID del cliente), `estado` (cadena con valor `PENDIENTE`), `descripcion` (cadena), `latitud` y `longitud` (números), y `fechaCreacion` y `fechaActualizacion` (marcas de tiempo). Para que aparezca en todos los flujos, usa como referencia una solicitud creada por la app con su categoría, presupuesto y ubicación. [Guía de la consola de Firestore](https://firebase.google.com/docs/firestore/using-console)

Para probar el panel de administración en un proyecto de desarrollo, registra una cuenta de prueba mediante la app. En Firestore, abre su documento `Usuario/{uid}`, edita `tipoUsuario` (cadena) a `ADMIN`, guarda y vuelve a iniciar sesión con esa cuenta. Ese campo determina la pantalla de destino. Configura el acceso a los datos mediante las reglas de Firestore.

El flujo actual de correo y contraseña guarda la contraseña directamente en `credenciales`. Usa únicamente cuentas ficticias con contraseñas desechables en desarrollo. Antes de usar cuentas reales, migra ese flujo a Firebase Authentication.

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