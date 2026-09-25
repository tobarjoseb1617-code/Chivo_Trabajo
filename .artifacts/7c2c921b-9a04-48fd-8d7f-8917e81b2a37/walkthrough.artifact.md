# Walkthrough - Funcionalidad de Firestore, Leaflet y Ruteo por Rol

Se ha conectado toda la lógica funcional del backend de Firebase Firestore, la integración del mapa interactivo con Leaflet API y el ruteo inteligente basado en roles.

## Changes Made

### 1. Mapa Interactivo Leaflet (OpenStreetMap) en Registro Cliente Paso 2
- **Asset HTML (`assets/leaflet_map.html`)**: Se creó un mapa interactivo con Leaflet.js que permite pinchar o arrastrar un marcador para fijar la ubicación exacta.
- **JavascriptInterface (`WebAppInterface`)**: Comunica las coordenadas de latitud y longitud seleccionadas en el mapa directamente con la Activity nativa de Android.
- **Firestore**: Al hacer clic en *"Completar Registro y Explorar"*, se guardan en el documento del usuario en Firestore (`Usuario`) los campos de `latitud`, `longitud`, `ciudad` (municipio) y `direccion`.

### 2. Ruteo Dinámico por Rol al Iniciar Sesión
- **`IniciarSesionActivity.kt`**: Al autenticar con correo o Google, consulta el perfil en Firestore y evalúa el campo `tipoUsuario`:
  - Si es `"PROFESIONAL"` ➔ Redirige a **`InicioProfesionalActivity`**.
  - Si es `"CLIENTE"` ➔ Redirige a **`InicioActivity`**.

---

## Verification Results

### Automated Tests
- Compilación del proyecto:
  ```bash
  > Task :app:assembleDebug
  BUILD SUCCESSFUL in 10s
  ```
- El proyecto compila limpiamente sin errores con la integración de WebView, Leaflet y Firestore.
