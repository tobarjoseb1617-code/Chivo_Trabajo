# Arquitectura del Sistema - ChivoTrabajo

ChivoTrabajo está desarrollado bajo los estándares más altos de desarrollo nativo en Android, implementando el patrón **MVVM (Model-View-ViewModel)** y **Repository Pattern**, organizados en paquetes modulares en idioma español para facilitar el mantenimiento y la escalabilidad.

---

## 🏛️ 1. Patrón Arquitectónico (MVVM + Repository)

La arquitectura se divide en tres capas principales:
1. **Capa de Vista (UI / Activities)**:
   - Responsable exclusivamente de renderizar las interfaces de usuario XML, gestionar ViewBinding, y escuchar los eventos de la ViewModel o LiveData.
   - Actividades independientes para cada pantalla (`InicioActivity`, `PerfilProfesionalActivity`, `ChatActivity`, etc.), garantizando un ciclo de vida limpio y desacoplado.
2. **Capa de Lógica de Negocio (ViewModels)**:
   - `AutenticacionViewModel`: Gestiona el estado de autenticación, registro de usuarios, inicio de sesión (correo y Google) y ruteo basado en roles.
3. **Capa de Datos (Data & Repositories)**:
   - `AuthRepository`: Interfaz con Firebase Authentication para el manejo de sesiones seguras.
   - `FirestoreRepository`: Comunicación asíncrona con Cloud Firestore mediante Corrutinas (`suspend` functions y `await()`) para la persistencia de perfiles de usuario.

---

## 📂 2. Distribución de Clases y Paquetes

```text
com.cherodevscode.chivo_trabajo/
│
├── data/
│   ├── model/                  # Modelos de datos (Esquema Firestore)
│   │   ├── Calificacion.kt     # Reseñas y calificaciones de servicios
│   │   ├── Categoria.kt        # Categorías de oficios (Fontanería, Electricidad, etc.)
│   │   ├── Chat.kt             # Mensajería y propuestas
│   │   ├── Pago.kt             # Transacciones y métodos de pago
│   │   ├── Portafolio.kt       # Trabajos realizados por profesionales
│   │   ├── Profesional.kt      # Perfil especializado del trabajador
│   │   ├── Registro.kt         # Solicitudes y ofertas
│   │   ├── Servicio.kt         # Estado de servicios activos/finalizados
│   │   ├── Solicitud.kt        # Datos de publicación de trabajos
│   │   └── Usuario.kt          # Esquema maestro de usuarios (Cliente / Profesional)
│   │
│   └── repository/             # Capa de abstracción de datos
│       ├── AuthRepository.kt   # Autenticación (Firebase Auth)
│       ├── DuiRepository.kt    # Validación de documentos de identidad
│       └── FirestoreRepository.kt # Operaciones CRUD en Firestore
│
├── ui/
│   ├── autenticacion/          # Flujo de Acceso y Registro
│   │   ├── AutenticacionViewModel.kt
│   │   ├── IniciarSesionActivity.kt
│   │   ├── RegistroActivity.kt (Paso 1)
│   │   ├── RegistroClientePaso2Activity.kt (Paso 2 - Cliente con Leaflet)
│   │   └── RegistroProfesionalPaso2Activity.kt (Paso 2 - Profesional)
│   │
│   ├── cliente/                # Módulo y Pantallas del Cliente
│   │   ├── CrearSolicitudActivity.kt
│   │   ├── DetallesSolicitudCreadaClienteActivity.kt
│   │   ├── ExplorarMapaActivity.kt
│   │   ├── HistorialDeServiciosActivity.kt
│   │   ├── HistorialSolicitudesClienteActivity.kt
│   │   ├── InicioActivity.kt
│   │   ├── ProfesionalesCercanosActivity.kt
│   │   ├── RadarProfesionalesActivity.kt
│   │   └── SeguimientoActivity.kt
│   │
│   ├── profesional/            # Módulo y Pantallas del Profesional
│   │   ├── DetallesSolicitudTrabajoProfesionalActivity.kt
│   │   ├── EjecucionActivity.kt
│   │   ├── GestionarServicioEnCursoActivity.kt
│   │   ├── InicioProfesionalActivity.kt
│   │   └── SolicitudesTrabajosCercanosActivity.kt
│   │
│   ├── chat_y_evaluacion/      # Módulo de Comunicación y Cierre
│   │   ├── ChatActivity.kt
│   │   ├── FinalizarYCalificarActivity.kt
│   │   └── HistorialChatsActivity.kt
│   │
│   └── perfil/                 # Módulo de Perfiles y Ajustes
│       ├── ConfiguracionPerfilActivity.kt
│       └── PerfilProfesionalActivity.kt
│
└── utils/                      # Utilidades del sistema
    └── PinGenerator.kt         # Generador de PINs de seguridad de 4 dígitos
```

---

## 🔄 3. Flujo de Ejecución del Sistema

1. **Autenticación y Ruteo Dinámico**:
   - El usuario inicia sesión (Correo/Contraseña o Google Sign-In) o se registra seleccionando su rol (`CLIENTE` o `PROFESIONAL`).
   - El sistema consulta Cloud Firestore para obtener el perfil y redirige automáticamente a la pantalla principal correspondiente:
     - `CLIENTE` ➔ `InicioActivity`
     - `PROFESIONAL` ➔ `InicioProfesionalActivity`
2. **Registro de Ubicación Geográfica (Leaflet API)**:
   - En el Paso 2 del cliente, se integra un `WebView` con Leaflet.js y OpenStreetMap para seleccionar la ubicación exacta en el mapa de El Salvador (`latitud` y `longitud`), guardándola en Firestore.
3. **Ciclo de Servicio y Seguridad (ChivoSeguro)**:
   - Publicación de solicitud con presupuesto y fotos.
   - Recepción de propuestas de técnicos cercanos.
   - Aceptación de propuesta y generación de PIN de seguridad de 4 dígitos (estilo Uber).
   - Ejecución, validación de PIN por el técnico, chat en tiempo real y calificación final con estrellas (5.0).
