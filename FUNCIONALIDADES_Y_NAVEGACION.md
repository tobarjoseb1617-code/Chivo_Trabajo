# Funcionalidades, Conexión de Clases y Estructura de Subcolecciones - ChivoTrabajo

Este documento detalla en profundidad el funcionamiento técnico de las características que ya se encuentran operativas: **Perfil**, **Registro**, **Creación de Portafolio** y **Visualización de Portafolio**, explicando las clases involucradas, su interconexión y cómo se estructuran los datos en subcolecciones en Cloud Firestore.

---

## 🏗️ 1. Arquitectura de Conexión entre Clases por Módulo

### A. Módulo de Registro Profesional (`registro`)
* **Clases Involucradas**:
  * `RegistroProfesionalPaso2Activity.kt` (Vista principal del formulario).
  * `CategoriasConfig.kt` (Objeto estático que provee las 20 categorías oficiales y sus sub-servicios asociados).
  * `Profesional.kt` (Modelo de datos).
  * `FirestoreRepository.kt` (Capa de persistencia).
* **Flujo y Conexión**:
  1. El usuario abre `RegistroProfesionalPaso2Activity`. Al tocar el campo de especialidad principal, la actividad consulta `CategoriasConfig.listaCategorias` para desplegar un `AlertDialog` de selección múltiple (`setMultiChoiceItems`).
  2. Al seleccionar las especialidades, la función `actualizarServiciosDinamicos()` lee de `CategoriasConfig.mapaCategoriasServicios` los sub-servicios correspondientes y los dibuja como botones interactivos en pantalla.
  3. Al completar el formulario y presionar el botón de registro, se construye un objeto `Profesional` con las listas de `especialidades` y `serviciosOfrecidos`.
  4. La actividad invoca a `FirestoreRepository.guardarProfesional(profesional)`, el cual realiza una petición asíncrona mediante Corrutinas hacia la colección `"profesionales"` en Firestore.

---

### B. Módulo de Perfil y Configuración (`perfil`)
* **Clases Involucradas**:
  * `ConfiguracionPerfilActivity.kt` (Panel de configuración de cuenta del usuario actual).
  * `PerfilProfesionalActivity.kt` (Vista pública del perfil profesional).
  * `AuthRepository.kt` (Gestión de sesión Firebase Auth).
  * `FirestoreRepository.kt` (Lectura de datos de usuario).
* **Flujo y Conexión**:
  1. `ConfiguracionPerfilActivity` se inicializa cargando la sesión activa (`AuthRepository.usuarioActual()`).
  2. Consulta a Firestore (`FirestoreRepository.obtenerPerfilUsuario`) para renderizar el nombre, correo, teléfono y foto de perfil (usando Glide).
  3. Contiene opciones para editar información, ver términos, cerrar sesión (limpiando tareas y redirigiendo al Login), y el botón **"Mi Portafolio"**.
  4. Al tocar "Mi Portafolio", se despliega un diálogo (`AlertDialog`) que permite elegir entre **Ver Portafolio** (`PortafolioProfesionalActivity`) o **Crear / Agregar al Portafolio** (`CrearPortafolioActivity`).

---

### C. Módulo de Portafolio: Creación y Visualización (`portafolio` y `ver portafolio`)
* **Clases Involucradas**:
  * `PortafolioProfesionalActivity.kt` (Pantalla de galería de trabajos publicados).
  * `CrearPortafolioActivity.kt` (Pantalla/Diálogos modales para subir fotos, títulos y descripciones).
  * `PortafolioAdapter.kt` (Adaptador del RecyclerView en cuadrícula de 2 columnas).
  * `Portafolio.kt` (Modelo de datos de un elemento del portafolio).
  * `item_portafolio_profesional.xml` (Diseño de tarjeta individual).
  * `FirestoreRepository.kt` (Operaciones CRUD de subcolecciones).
* **Flujo y Conexión**:
  1. **Visualización (`PortafolioProfesionalActivity`)**: Al abrirse, invoca en un hilo secundario de Corrutinas a `FirestoreRepository.obtenerPortafolio(uid)`. Esta función consulta la **subcolección anidada** en Firestore (`profesionales/{uid}/Portafolio`). Los datos obtenidos se pasan a `PortafolioAdapter`, el cual renderiza cada trabajo en una cuadrícula mediante Glide.
  2. **Creación / Adición (`CrearPortafolioActivity` o Diálogos Modales)**:
     - El profesional selecciona una imagen desde su galería (`ActivityResultContracts.GetContent()`), la cual se previsualiza instantáneamente con Glide.
     - Ingresa un título y una descripción detallada.
     - Al presionar guardar, la imagen se sube mediante una petición HTTP Multipart a la API de **Cloudinary** utilizando múltiples presets de respaldo (`chivo_trabajo`, etc.) para obtener su `secure_url`.
     - Una vez obtenida la URL de Cloudinary, se invoca a `FirestoreRepository.guardarTrabajoPortafolio(uid, url, titulo, descripcion)`.

---

## 🗄️ 2. ¿Cómo se Guarda el Portafolio en Subcolecciones en Firestore?

En Cloud Firestore, la estructura relacional jerárquica se implementa mediante **Subcolecciones**. El portafolio de cada profesional no se guarda en una colección plana independiente, sino anidado directamente dentro del documento de su perfil profesional.

### Estructura de Rutas en Firestore:
```text
/profesionales/{uidProfesional} (Documento principal del profesional)
│
├── aniosExperiencia: 5
├── calificacionPromedio: 4.9
├── descripcion: "Técnico especialista..."
├── especialidad: "Fontanería y Tuberías"
├── especialidades: ["Fontanería y Tuberías", "Electricidad Residencial"]
└── /Portafolio (Subcolección)
    │
    ├── {idFoto_1} (Documento con ID autogenerado por Firestore)
    │   ├── idFoto: "NHb8MmAkfSsImSQoVBL"
    │   ├── titulo: "Instalación de Bomba Hidroneumática"
    │   ├── descripcion: "Sustitución de equipo antiguo y pruebas de presión."
    │   ├── url: "https://res.cloudinary.com/..."
    │   └── fecha: Timestamp(...)
    │
    └── {idFoto_2}
        ├── idFoto: "XyZ789abc..."
        ├── titulo: "Certificado Técnico ITCA"
        ├── descripcion: "Acreditación / Título Profesional"
        ├── url: "https://res.cloudinary.com/..."
        └── fecha: Timestamp(...)
```

### Funciones Clave en `FirestoreRepository.kt` para las Subcolecciones:
1. **Guardar Trabajo (`guardarTrabajoPortafolio`)**:
   ```kotlin
   val documento = db.collection("profesionales")
       .document(uidProfesional)
       .collection("Portafolio")
       .document() // Genera un ID único automático
   
   val trabajo = Portafolio(idFoto = documento.id, url = url, titulo = titulo, descripcion = descripcion)
   documento.set(trabajo).await()
   ```
2. **Obtener Portafolio (`obtenerPortafolio`)**:
   ```kotlin
   val documentos = db.collection("profesionales")
       .document(uidProfesional)
       .collection("Portafolio")
       .get()
       .await()
   // Mapea los documentos a una lista de objetos Portafolio
   ```
3. **Eliminar / Actualizar Trabajo**:
   Se apunta directamente al documento específico dentro de la subcolección:
   ```kotlin
   db.collection("profesionales").document(uid).collection("Portafolio").document(idFoto).delete().await()
   ```
