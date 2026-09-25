# Arquitectura y Documentación Técnica - ChivoTrabajo

ChivoTrabajo es una aplicación móvil nativa para Android desarrollada en **Kotlin** utilizando el patrón arquitectónico **MVVM (Model-View-ViewModel)** y el **Repository Pattern**, organizada en una estructura modular en español optimizada para la plataforma de servicios técnicos en El Salvador.

---

## 🏗️ 1. Estructura de Paquetes (Modularización en Español)

El proyecto está estructurado bajo el namespace `com.cherodevscode.chivo_trabajo`:

```text
com.cherodevscode.chivo_trabajo/
│
├── data/
│   ├── model/                  # Modelos de datos (Entidades Firestore)
│   │   ├── Calificacion.kt
│   │   ├── Categoria.kt
│   │   ├── Chat.kt
│   │   ├── Pago.kt
│   │   ├── Portafolio.kt
│   │   ├── Profesional.kt
│   │   ├── Registro.kt
│   │   ├── Servicio.kt
│   │   ├── Solicitud.kt
│   │   └── Usuario.kt          # Esquema oficial de usuarios Firestore
│   │
│   └── repository/             # Repositorios de datos
│       ├── AuthRepository.kt   # Autenticación Firebase (Email & Google)
│       ├── DuiRepository.kt    # Validación de documentos de identidad
│       └── FirestoreRepository.kt # Operaciones CRUD en Cloud Firestore
│
├── ui/
│   ├── autenticacion/          # Módulo de Autenticación y Registro
│   │   ├── AutenticacionViewModel.kt
│   │   ├── IniciarSesionActivity.kt
│   │   ├── RegistroActivity.kt (Paso 1)
│   │   ├── RegistroClientePaso2Activity.kt (Paso 2 - Cliente con Leaflet)
│   │   └── RegistroProfesionalPaso2Activity.kt (Paso 2 - Profesional)
│   │
│   ├── cliente/                # Módulo del Cliente (Hogar / Empresas)
│   │   ├── CrearSolicitudActivity.kt
│   │   ├── ExplorarMapaActivity.kt
│   │   ├── InicioActivity.kt   # Dashboard Principal del Cliente
│   │   ├── RadarProfesionalesActivity.kt
│   │   └── SeguimientoActivity.kt # Tracking y PIN de seguridad (Estilo Uber)
│   │
│   ├── profesional/            # Módulo del Profesional (Técnico / Especialista)
│   │   ├── EjecucionActivity.kt # Validación de PIN por el profesional
│   │   ├── GestionarServicioEnCursoActivity.kt # Detalle de orden y fases
│   │   ├── InicioProfesionalActivity.kt # Dashboard del Profesional
│   │   └── SolicitudesTrabajosCercanosActivity.kt # Feed de solicitudes
│   │
│   ├── chat_y_evaluacion/      # Módulo de Comunicación y Cierre
│   │   ├── ChatActivity.kt     # Chat Detalle con propuesta legal fijada
│   │   └── FinalizarYCalificarActivity.kt # Factura, pago y calificación de 5 estrellas
│   │
│   └── perfil/                 # Módulo de Perfiles
│       └── PerfilProfesionalActivity.kt # Perfil PRO, portafolio y garantías
│
└── utils/                      # Utilidades del sistema
    └── PinGenerator.kt         # Generador de PINs de seguridad
```

---

## 👥 2. División por Bloques (Cliente vs. Profesional)

### 🔵 Bloque Cliente (`ui.cliente`)
Diseñado para usuarios que requieren reparaciones, mantenimiento o proyectos en su hogar u oficina con garantía y validación por DUI.
* **Dashboard (`InicioActivity`)**: Muestra saludo con DUI verificado, tarjeta de servicio en curso (con estado "En camino" y botón de mapa), cuadrícula de categorías populares con conteo de técnicos cercanos, garantía ChivoSeguro y profesionales destacados (Roberto Méndez, David Henríquez).
* **Exploración y Radar (`ExplorarMapaActivity`, `RadarProfesionalesActivity`)**: Mapas interactivos con Leaflet API, filtros por distancia (`<3km`), valoración y disponibilidad en tiempo real.
* **Creación de Solicitudes (`CrearSolicitudActivity`)**: Formulario de 5 pasos para detallar el problema, adjuntar fotos, confirmar ubicación, seleccionar urgencia y definir rangos de presupuesto.
* **Seguimiento y PIN (`SeguimientoActivity`)**: Pantalla de tracking con generación automática de PIN de 4 dígitos para validar la llegada del técnico.
* **Cierre y Calificación (`FinalizarYCalificarActivity`)**: Recibo de pago transparente, selección de método de pago (Efectivo / Transferencia QR) y reseña de 5 estrellas con atributos destacados.

### 🟠 Bloque Profesional (`ui.profesional`)
Diseñado para técnicos y especialistas certificados que desean recibir alertas, postularse a trabajos y gestionar servicios.
* **Dashboard Profesional (`InicioProfesionalActivity`)**: Panel con interruptor de disponibilidad en vivo ("Disponible para trabajar"), tarjeta de servicio en curso con tarifa pactada ($25.00 USD) y feed de oportunidades en vivo (urgentes y programados).
* **Feed de Solicitudes (`SolicitudesTrabajosCercanosActivity`)**: Listado de trabajos cercanos con radio de 5 km, fotos adjuntas de los clientes, presupuesto estimado y botones de cotización u oferta directa.
* **Gestión de Servicio (`GestionarServicioEnCursoActivity`)**: Control por fases (*Aceptado ➔ En camino ➔ Llegada ➔ Trabajo ➔ Cobro*), navegación GPS/Waze, validación de ingreso residencial con DUI y registro de evidencias fotográficas (Antes y Después).
* **Validación de Ejecución (`EjecucionActivity`)**: Ingreso del PIN de 4 dígitos proporcionado por el cliente para iniciar formalmente la mano de obra.

---

## 🚀 3. Funcionalidades Actuales del Proyecto

El sistema se encuentra 100% funcional y conectado a **Firebase**:
1. **Autenticación Multi-Rol**: Registro con selección de rol (`CLIENTE` o `PROFESIONAL`), correo/contraseña y Google Sign-In.
2. **Esquema Cloud Firestore (`Usuario`)**:
   * Almacena `uid`, `nombre`, `apellido`, `correo`, `telefono`, `tipoUsuario`, `latitud`, `longitud`, `direccion`, `ciudad`, `fechaRegistro` y `activo`.
3. **Mapa Leaflet API (OpenStreetMap)**:
   * Integrado mediante `WebView` y `JavascriptInterface` en el registro de ubicación del cliente para capturar coordenadas exactas (`latitud` y `longitud`).
4. **Ruteo Dinámico por Rol**:
   * Al iniciar sesión, la app evalúa el campo `tipoUsuario` en Firestore y enruta automáticamente a `InicioActivity` (Cliente) o `InicioProfesionalActivity` (Profesional).
5. **Sistema de Seguridad ChivoSeguro**:
   * Validación de DUI, fondos protegidos en garantía por 30 días, y PINs de seguridad de 4 dígitos para inicio de obra.
