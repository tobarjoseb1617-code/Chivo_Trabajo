# Plan de Implementación: Pantalla de Seguimiento y Propuestas (`activity_detalles_solicitud_creada_cliente.xml`)

Crear la pantalla de **Seguimiento y Propuestas del Cliente** (`DetallesSolicitudCreadaClienteActivity.kt` y `activity_detalles_solicitud_creada_cliente.xml`), basada exactamente en la imagen de referencia del seguimiento de la solicitud creada con el listado de propuestas de profesionales recibidas.

## User Review Required

> [!IMPORTANT]
> Este diseño incluirá:
> - Barra superior con flecha de retroceso, título *"Seguimiento En Vivo"* y avatar.
> - Cabecera de la solicitud (#SOL-8942, 3 propuestas, *"Reparación de Fuga en Fregadero"*).
> - Tarjeta de **Detalle del Problema** (Fontanería, descripción y 2 fotos de evidencia).
> - Tarjeta de **Ubicación y Horario** (Residencial Cumbres de Cuscatlán, hoy entre 1:00 PM – 3:30 PM).
> - Tarjeta de **Presupuesto estimado** ($20.00 – $35.00 USD, Efectivo / Transferencia).
> - Sección de **Propuestas recibidas** (3 técnicos ordenados por cercanía):
>   - **Roberto Méndez** (Más Recomendado, Maestro Fontanero, 4.9 ★, DUI Verificado, Llega en 15 min, Oferta: **$25.00 USD**, comentario con detalle de materiales, botones *Perfil*, *Chatear* y *Aceptar*).
>   - **Carlos Mendoza** (4.8 ★, En 25 min, $28.00 USD, botón *Ver Propuesta*).
>   - **David Ramos** (4.7 ★, En 45 min, $22.00 USD, botón *Ver Propuesta*).
> - Botones inferiores de acción: **Editar** y **Cancelar**.

## Proposed Changes

### 1. Interfaz de Usuario (`res/layout/activity_detalles_solicitud_creada_cliente.xml`)
- Crear el layout XML con el detalle de solicitud, fotos, horario y listado de propuestas de técnicos.

### 2. Activity (`ui/cliente/DetallesSolicitudCreadaClienteActivity.kt`)
- Crear la Activity para gestionar la aceptación de propuestas, chat con profesionales y edición/cancelación de la solicitud.

### 3. Registro en Manifiesto
- Registrar `DetallesSolicitudCreadaClienteActivity` en el `AndroidManifest.xml`.

## Verification Plan

### Automated Tests
- Compilación completa (`app:assembleDebug`) para validar que el layout, la Activity y el Manifiesto compilen sin errores.
