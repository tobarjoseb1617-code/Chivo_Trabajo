# Plan de Implementación: Pantalla de Solicitudes de Trabajos Cercanos (`activity_solicitudes_trabajos_cercanos.xml`)

Crear la pantalla de **Solicitudes de Trabajos Cercanos** (`SolicitudesTrabajosCercanosActivity.kt` y `activity_solicitudes_trabajos_cercanos.xml`), basada exactamente en la imagen de referencia del feed de solicitudes del profesional.

## User Review Required

> [!IMPORTANT]
> Este diseño incluirá:
> - Barra superior con ChivoTrabajo [PRO], ubicación, campana y avatar.
> - Banner superior de propuestas (*"2 propuestas enviadas hoy • 1 cliente revisando tu perfil"*).
> - Selector de zona y radio (*"San Salvador & La Li..."*, *"Radio: 5 km"*).
> - Filtros rápidos (`Todas (6)`, `Urgentes Hoy (3)`, `Fontanería General`).
> - Feed de solicitudes de trabajo:
>   - *Fuga de agua bajo fregadero de cocina* (Urgente, $15 – $30, Carlos Rivera + DUI, foto adjunta, botones "Detalles" y "Cotizar").
>   - *Mantenimiento de bomba de agua y presurizador* (Mañana, $40 – $70, Mariana Palacios + DUI, botón "Enviar Oferta Personalizada").
>   - *Cambio de 3 mezcladoras de lavamanos y llaves angulares* (Esta semana, Tarifa fija $35 USD, Roberto Alvarenga, botón "Aceptar Tarifa & Postular").
> - Banner inferior de activación de alertas por WhatsApp (*"¿No quieres perder clientes?"*).
> - Barra de navegación inferior del profesional con **"Solicitudes"** seleccionado.

## Proposed Changes

### 1. Interfaz de Usuario (`res/layout/activity_solicitudes_trabajos_cercanos.xml`)
- Crear el layout XML con el feed completo de solicitudes y tarjetas detalladas.

### 2. Activity (`ui/profesional/SolicitudesTrabajosCercanosActivity.kt`)
- Crear la Activity para gestionar las acciones de cotizar, ofertar, aceptar tarifas y activar alertas.

### 3. Registro en Manifiesto
- Registrar `SolicitudesTrabajosCercanosActivity` en el `AndroidManifest.xml`.

## Verification Plan

### Automated Tests
- Compilación completa (`app:assembleDebug`) para validar que el layout, la Activity y el Manifiesto compilen sin errores.
