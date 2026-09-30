# Plan de Implementación: Múltiples Especialidades y Servicios Específicos Dinámicos

Actualizar el flujo de registro profesional (`RegistroProfesionalPaso2Activity`) para permitir que el especialista seleccione **múltiples especialidades/categorías** (en lugar de una sola), mostrar y actualizar dinámicamente los **servicios específicos** correspondientes a esas especialidades seleccionadas, y persistir tanto las especialidades múltiples como los sub-servicios en Firestore (`Profesional.kt`).

## User Review Required

> [!IMPORTANT]
> Se implementarán los siguientes cambios clave:
> 1. **Selección Múltiple de Especialidades (`CategoriasConfig.kt` & Activity)**:
     - El profesional podrá seleccionar una o más categorías de la lista oficial de 20 mediante un selector de selección múltiple (`setMultiChoiceItems`).
     - Se mostrarán visualmente las especialidades seleccionadas (con chips o lista con opción de eliminar).
   2. **Servicios Específicos Dinámicos**:
     - Conforme el profesional elija sus especialidades, el contenedor de "Servicios específicos" se actualizará dinámicamente agrupando y mostrando los sub-servicios de todas las especialidades seleccionadas para que pueda marcarlos.
   3. **Modelo de Datos y Firestore (`Profesional.kt` & `FirestoreRepository.kt`)**:
     - Actualizar el modelo `Profesional` para soportar una lista de especialidades (`especialidades: List<String>`) además de `serviciosOfrecidos: List<String>`, asegurando que se guarden correctamente en la colección `"profesionales"`.
   4. **Comentarios Claros y Breves**:
     - Todo código nuevo y modificado irá debidamente comentado.

## Proposed Changes

### 1. Configuración de Categorías y Sub-servicios (`utils/CategoriasConfig.kt`)
- Actualizar `CategoriasConfig` para incluir un mapa (`Map<String, List<String>>`) que relacione cada categoría con sus sub-servicios específicos.

### 2. Modelo de Datos (`data/model/Profesional.kt`)
- Añadir el campo `especialidades: List<String>` (manteniendo compatibilidad con `especialidad: String` si es necesario).

### 3. Actividad de Registro Profesional (`RegistroProfesionalPaso2Activity.kt`)
- Implementar diálogo de selección múltiple para especialidades y generación dinámica de chips de sub-servicios.

## Verification Plan

### Automated Tests
- Compilación completa (`app:assembleDebug`) para validar que los modelos, repositorios y actividades compilen sin errores.
