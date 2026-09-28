# Funcionalidades y Sistema de Navegación - ChivoTrabajo

Este documento detalla todas las características que ya se encuentran operativas en la aplicación y cómo está estructurado el sistema de navegación para ambos roles.

---

## 📱 1. ¿Qué Funciona Actualmente en la Aplicación?

### A. Módulo de Autenticación y Registro (`ui.autenticacion`)
* **Inicio de Sesión**: Soporte para autenticación por correo/contraseña y Google Sign-In con Firebase Auth.
* **Ruteo Inteligente por Rol**: Al iniciar sesión, la app detecta si el usuario es `CLIENTE` o `PROFESIONAL` en Firestore y lo redirige a su respectivo panel principal.
* **Registro en 2 Pasos**:
  * **Paso 1**: Selección de perfil (Cliente u 호speda/Profesional) y datos personales básicos (Nombre, Apellido, Correo, Teléfono, Contraseña con visor ocular).
  * **Paso 2 (Cliente)**: Selección de dirección exacta, municipio y mapa interactivo con **Leaflet API / OpenStreetMap** para capturar `latitud` y `longitud` precisas.
  * **Paso 2 (Profesional)**: Configuración de oficio principal, servicios específicos, años de experiencia y zona de cobertura geográfica.

### B. Módulo del Cliente (`ui.cliente`)
* **Dashboard Principal (`InicioActivity`)**: Saludo con DUI verificado, tarjeta de servicio en curso en tiempo real, buscador, acceso a categorías populares (que abren el radar de profesionales), y tarjeta de profesionales cercanos con botones directos para chatear o ver perfil.
* **Creación de Solicitudes (`CrearSolicitudActivity`)**: Formulario completo de detalles, adjuntar fotos de evidencia, ubicación confirmada y presupuesto estimado.
* **Seguimiento y PIN (`SeguimientoActivity`)**: Pantalla de tracking y generación de código PIN de seguridad de 4 dígitos.
* **Radar y Exploración (`ExplorarMapaActivity`, `RadarProfesionalesActivity`, `ProfesionalesCercanosActivity`)**: Mapas interactivos, filtros por distancia y listados de profesionales certificados.
* **Mis Solicitudes (`HistorialSolicitudesClienteActivity`)**: Panel de solicitudes abiertas, con propuestas recibidas, en proceso y finalizadas.
* **Servicios (`HistorialDeServiciosActivity`)**: Listado de servicios activos e historial reciente con recibos y opción de reordenar.

### C. Módulo del Profesional (`ui.profesional`)
* **Dashboard Profesional (`InicioProfesionalActivity`)**: Panel con interruptor de disponibilidad en vivo, tarjeta de servicio en curso (con tarifa pactada y botón de ruta GPS) y feed de oportunidades en vivo.
* **Solicitudes Cercanas (`SolicitudesTrabajosCercanosActivity`)**: Listado de trabajos en la zona con radio de 5 km, fotos adjuntas de los clientes y botones de cotización u oferta directa.
* **Gestión de Servicio (`GestionarServicioEnCursoActivity`)**: Control por fases (*Aceptado ➔ En camino ➔ Llegada ➔ Trabajo ➔ Cobro*), navegación GPS/Waze, validación de ingreso residencial con DUI y registro de evidencias fotográficas (Antes y Después).
* **Validación de Ejecución (`EjecucionActivity`)**: Validación mediante el PIN de 4 dígitos proporcionado por el cliente.

### D. Módulo de Comunicación y Perfil (`ui.chat_y_evaluacion`, `ui.perfil`)
* **Chat Detalle (`ChatActivity`)**: Mensajería en tiempo real con tarjeta fijada de Propuesta de Servicio (Acuerdo Legal Chivo) y estados de lectura.
* **Historial de Chats (`HistorialChatsActivity`)**: Bandeja de entrada con conversaciones activas y finalizadas.
* **Trabajo Completado y Calificación (`FinalizarYCalificarActivity`)**: Recibo de pago transparente con desglose de mano de obra y repuestos, selección de método de pago (Efectivo / QR), y calificación de 5 estrellas con atributos destacables.
* **Configuración y Perfil PRO (`ConfiguracionPerfilActivity`, `PerfilProfesionalActivity`)**: Visualización y edición de datos personales, portafolio de 6 proyectos garantizados y cierre de sesión seguro.

---

## 🧭 2. Sistema de Navegación e Interfaz

### A. Menú de Navegación Inferior Estándar (5 Botones)
Todas las pantallas principales de la aplicación cuentan con exactamente **5 botones** en la barra de navegación inferior, organizados uniformemente en el siguiente orden:
1. 🏠 **Inicio**
2. 📋 **Solicitudes**
3. 🛠️ **Servicios**
4. 💬 **Mensajes**
5. 👤 **Perfil**

### B. Botón de Retroceso Global y Funcional
* **Flecha de Retorno**: Todas las pantallas secundarias y de detalle cuentan con un botón de retroceso superior (`finish()`) optimizado con área táctil amplia (40x40dp) y fondo de toque, garantizando un retorno limpio y sin cierres inesperados de la aplicación.
* **Pantallas sin Menú Inferior (Exploración / Radar)**: Las pantallas de mapas y radares se abren mediante botones secundarios y disponen de su respectiva flecha de retorno superior para volver al dashboard anterior.
