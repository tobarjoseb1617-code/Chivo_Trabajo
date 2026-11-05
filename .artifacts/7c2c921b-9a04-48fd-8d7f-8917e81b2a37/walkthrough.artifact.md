# Walkthrough - Uso Exclusivo de Colección "credenciales" sin Firebase Auth

Se ha configurado el repositorio de autenticación (`AuthRepository.kt`) para trabajar 100% con la colección **`credenciales`** en Cloud Firestore y sesión local mediante `SharedPreferences`, eliminando Firebase Authentication.

## Changes Made

### 1. Repositorio de Autenticación (`AuthRepository.kt`)
- Operaciones de registro e inicio de sesión consultando y guardando directamente en la colección `"credenciales"` de Firestore.
- Sesión persistente mediante `SharedPreferences` con el modelo `LocalUser`.
- Soporte para inicio de sesión con Google sincronizado con la colección `"credenciales"`.

---

## Verification Results

### Automated Tests
- Compilación del proyecto:
  ```bash
  > Task :app:assembleDebug
  BUILD SUCCESSFUL in 10s
  ```
- El proyecto compila limpiamente sin errores.
