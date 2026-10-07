# Walkthrough - Correcciones en Creación de Solicitudes y Mapa

Se han implementado todas las correcciones solicitadas en `CrearSolicitudActivity` y `SeleccionarUbicacionActivity`.

## Changes Made

### 1. Selección Múltiple de Fotos y Previsualización (`CrearSolicitudActivity.kt`)
- Se actualizó el selector de fotografías a `ActivityResultContracts.GetMultipleContents()` (hasta 5 fotos simultáneas).
- Se implementó la previsualización dinámica en miniatura directamente en la galería de fotos de la pantalla antes de publicar.

### 2. Sombreado Sutil de Selección en Tarjetas
- Se actualizaron `destacarCardFecha` y `destacarCardPresupuesto` para usar un tinte semitransparente sutil (`#26FFFFFF`) que resalta la selección claramente sin volver el fondo blanco ni ocultar el texto de las tarjetas.

### 3. Buscador del Mapa (`activity_seleccionar_ubicacion.xml`)
- Se configuró el campo de búsqueda de direcciones (`etBuscarUbicacion`) con texto en color blanco (`#FFFFFF`) para una perfecta legibilidad sobre el fondo.

---

## Verification Results

### Automated Tests
- Compilación del proyecto:
  ```bash
  > Task :app:assembleDebug
  BUILD SUCCESSFUL in 10s
  ```
- El proyecto compila limpiamente sin errores.
