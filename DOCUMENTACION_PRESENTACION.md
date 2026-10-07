# Documentación de Presentación - ChivoTrabajo

Este documento está preparado como guía técnica y conceptual para la presentación del proyecto **ChivoTrabajo**. Contiene la arquitectura, el desglose de carpetas y clases, el flujo lógico de funcionamiento y una sección especial con **posibles preguntas y respuestas** para la defensa.

---

## 🛠️ 1. Ficha Técnica

1. **Lenguaje & IDE**: Kotlin, Android Studio.
2. **Servicios en la Nube**: 
   * **Firebase Authentication**: Autenticación segura de usuarios mediante correo/contraseña y Google Sign-In.
   * **Cloud Firestore**: Base de datos NoSQL en tiempo real para almacenar perfiles, solicitudes, catálogos y subcolecciones de portafolio.
   * **Cloudinary**: API externa para la subida, alojamiento y optimización de imágenes (fotos de perfil, fotos de trabajos en el portafolio y acreditaciones).
   * **API de Mapas (Leaflet.js + OpenStreetMap)**: Integrada mediante un componente `WebView` nativo para la selección y geolocalización de coordenadas (latitud y longitud) en El Salvador.
3. **Librerías & Patrón de Diseño**:
   * **MVVM (Model-View-ViewModel)** + **Repository Pattern**.
   * **ViewModel & LiveData**: Gestión reactiva del ciclo de vida de la interfaz.
   * **Kotlin Coroutines**: Programación asíncrona no bloqueante para llamadas de red y base de datos.
   * **ViewBinding**: Vinculación segura de vistas XML sin `findViewById`.
   * **Glide**: Carga, caché y transformación circular de imágenes.

---

## 📂 2. Estructura de Carpetas y Explicación de Clases

El código fuente se encuentra organizado en `com.cherodevscode.chivo_trabajo/`:

### 📁 `data/model/` (Modelos de Datos / Esquema NoSQL)
* `Calificacion.kt`: Estructura para las reseñas y puntuaciones de 1 a 5 estrellas dejadas a los profesionales.
* `Categoria.kt`: Representación de las categorías de servicios.
* `Chat.kt`: Modelo para la mensajería en tiempo real y propuestas comerciales.
* `Pago.kt`: Transacciones, métodos de pago (Efectivo/QR) y montos de mano de obra y repuestos.
* `Portafolio.kt`: Trabajos realizados por el profesional (idFoto, url de Cloudinary, titulo, descripcion y fecha).
* `Profesional.kt`: Perfil especializado (especialidad principal, lista de especialidades múltiples, servicios ofrecidos, zona de trabajo, años de experiencia).
* `Registro.kt`: Solicitudes de servicios publicadas por los clientes.
* `Servicio.kt`: Estado actual de los servicios activos o finalizados.
* `Solicitud.kt`: Datos detallados de las ofertas y solicitudes en curso.
* `Usuario.kt`: Esquema maestro de usuarios (uid, nombre, apellido, correo, teléfono, tipoUsuario `CLIENTE` o `PROFESIONAL`, estado de verificación DUI, foto de perfil, lat/lng).

### 📁 `data/repository/` (Capa de Abstracción de Datos)
* `AuthRepository.kt`: Gestiona la sesión actual con Firebase Auth (inicio de sesión, registro y cierre de sesión).
* `DuiRepository.kt`: Lógica de validación de identidad y documentos.
* `FirestoreRepository.kt`: Encargado de todas las consultas CRUD hacia CloudFirestore utilizando Corrutinas de Kotlin (`suspend` functions), incluyendo la gestión de perfiles y subcolecciones de portafolio.

### 📁 `ui/autenticacion/` (Flujo de Acceso y Registro)
* `AutenticacionViewModel.kt`: ViewModel que procesa la lógica de autenticación de forma reactiva.
* `IniciarSesionActivity.kt`: Pantalla principal de acceso (Email/Password y Google Sign-In).
* `RegistroActivity.kt` (Paso 1): Creación de cuenta y selección inicial del rol (Cliente o Profesional).
* `RegistroClientePaso2Activity.kt` (Paso 2 - Cliente): Configuración de dirección y mapa interactivo (Leaflet) para geolocalización.
* `RegistroProfesionalPaso2Activity.kt` (Paso 2 - Profesional): Selección múltiple estricta de las 20 categorías oficiales (`CategoriasConfig`), servicios específicos dinámicos, años de experiencia y zonas de cobertura.

### 📁 `ui/cliente/` (Módulo del Cliente)
* `InicioActivity.kt`: Dashboard principal del cliente (servicios activos, profesionales cercanos, categorías populares con acceso al radar).
* `CrearSolicitudActivity.kt`: Formulario para publicar nuevas solicitudes de servicio con fotos y presupuesto.
* `DetallesSolicitudCreadaClienteActivity.kt`: Seguimiento en vivo de solicitudes y recepción de propuestas de técnicos.
* `ExplorarMapaActivity.kt` / `RadarProfesionalesActivity.kt` / `ProfesionalesCercanosActivity.kt`: Mapas interactivos y radares concéntricos de técnicos en la zona.
* `HistorialSolicitudesClienteActivity.kt`: Panel "Mis Solicitudes" (abiertas, en proceso, finalizadas).
* `HistorialDeServiciosActivity.kt`: Listado de servicios activos e historial con recibos.
* `SeguimientoActivity.kt`: Pantalla de tracking con PIN de seguridad de 4 dígitos.

### 📁 `ui/profesional/` (Módulo del Profesional)
* `InicioProfesionalActivity.kt`: Dashboard del profesional (interruptor de disponibilidad, servicio en curso, oportunidades en vivo).
* `SolicitudesTrabajosCercanosActivity.kt`: Feed de trabajos disponibles en la zona (radio de 5 km) con cotización rápida.
* `GestionarServicioEnCursoActivity.kt`: Control por fases (*En camino ➔ Llegada ➔ Trabajo ➔ Cierre*), GPS/Waze y validación de DUI.
* `DetallesSolicitudTrabajoProfesionalActivity.kt`: Visualización de detalles de solicitud y envío de propuesta económica.
* `EjecucionActivity.kt`: Validación del PIN de seguridad proporcionado por el cliente.
* `PortafolioProfesionalActivity.kt` / `CrearPortafolioActivity.kt`: Galería de trabajos publicados (con opción de agregar trabajos y títulos mediante diálogos modales) y subida de imágenes a Cloudinary.

### 📁 `ui/chat_y_evaluacion/`, `ui/perfil/` y `utils/`
* `ChatActivity.kt`: Mensajería en tiempo real y tarjeta de propuesta comercial.
* `HistorialChatsActivity.kt`: Bandeja de entrada con indicador de rol.
* `FinalizarYCalificarActivity.kt`: Resumen de costos, método de pago y calificación de 5 estrellas.
* `ConfiguracionPerfilActivity.kt` / `PerfilProfesionalActivity.kt`: Ajustes de cuenta, edición de perfil, acceso a portafolio y cierre de sesión seguro.
* `CategoriasConfig.kt`: Objeto utilitario con las 20 categorías oficiales de El Salvador y sus sub-servicios específicos asociados.

---

## 🔄 3. Flujo Lógico de la Aplicación

1. **Arranque y Autenticación**:
   - El usuario abre la app (`IniciarSesionActivity`). Si ya tiene sesión activa, se valida su rol en Firestore (`Usuario.tipoUsuario`).
2. **Ruteo por Rol**:
   - Si es **Cliente** ➔ Redirige a `InicioActivity`.
   - Si es **Profesional** ➔ Redirige a `InicioProfesionalActivity`.
3. **Navegación Unificada (5 Botones)**:
   - Ambas interfaces disponen de una barra inferior estricta de 5 opciones (`Inicio`, `Solicitudes`, `Servicios`, `Mensajes`, `Perfil`), asegurando que la navegación nunca quede bloqueada.
4. **Ciclo de Vida del Servicio**:
   - Cliente publica solicitud ➔ Profesional recibe oferta en `SolicitudesTrabajosCercanos` ➔ Envía cotización ➔ Cliente acepta ➔ Se genera PIN de 4 dígitos ➔ Profesional valida PIN (`EjecucionActivity`) ➔ Servicio en curso ➔ Cierre de contrato, desglose de pagos y calificación con estrellas.

---

## ❓ 4. Posibles Preguntas de la Presentación y sus Respuestas Técnicas

### P1: ¿Por qué eligieron el patrón MVVM en lugar de MVC o MVP?
* **R/**: Porque MVVM (Model-View-ViewModel) desacopla por completo la interfaz de usuario de la lógica de negocio. Gracias al uso de `ViewModel` y `LiveData`, la UI reacciona automáticamente a los cambios de estado de los datos, lo que evita fugas de memoria (*Memory Leaks*) al rotar la pantalla y facilita enormemente las pruebas unitarias.

### P2: ¿Cómo manejan la asincronía en las consultas a Firebase y Cloudinary sin congelar la interfaz?
* **R/**: Utilizamos **Kotlin Coroutines**. Todas las operaciones de red, peticiones HTTP a la API de Cloudinary y consultas a Cloud Firestore se ejecutan en hilos secundarios utilizando `Dispatchers.IO`. Una vez finalizada la tarea, el resultado se regresa al hilo principal con `Dispatchers.Main` para actualizar la interfaz de manera fluida y sin bloqueos de UI (*Jank*).

### P3: ¿Cómo funciona la geolocalización sin requerir una API de pago como Google Maps?
* **R/**: Integramos **Leaflet.js** y **OpenStreetMap** dentro de un componente nativo **`WebView`** en Android. Esto nos permite renderizar mapas interactivos de alta calidad, permitir la selección de pines y capturar las coordenadas (`latitud` y `longitud`) de manera completamente gratuita y sin depender de claves de API de pago de Google.

### P4: ¿Cómo garantizan que un profesional no ingrese oficios inválidos o duplicados?
* **R/**: Implementamos una clase de configuración estática (`CategoriasConfig.kt`) que define **20 categorías profesionales oficiales** en El Salvador. En la pantalla de registro (`RegistroProfesionalPaso2Activity`), el campo de especialidad está bloqueado para escritura libre y despliega un selector modal estricto (`AlertDialog` con selección múltiple), garantizando que todos los registros estén estandarizados.

### P5: ¿Cómo se maneja la seguridad en el inicio de un servicio entre el cliente y el profesional?
* **R/**: Implementamos un sistema de **PIN de seguridad de 4 dígitos** (similar al modelo de transporte privado). Cuando el profesional llega a la ubicación del cliente, el cliente le proporciona su PIN único de 4 dígitos. El profesional lo ingresa en su pantalla de ejecución (`EjecucionActivity`), validando de forma segura que ambas partes están presentes antes de iniciar el trabajo en curso.
