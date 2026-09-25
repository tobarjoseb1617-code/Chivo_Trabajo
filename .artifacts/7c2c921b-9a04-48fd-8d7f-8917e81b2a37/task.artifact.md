# Tareas de Ejecución: Flujo Limpio de Login, Registro, Recuperación y DUI

- [x] Crear plan de implementación (`implementation_plan.artifact.md`) y obtener aprobación del usuario.
- [x] **Paso 1**: Actualizar `AuthRepository.kt` agregando el método de recuperación de contraseña (`enviarCorreoRecuperacionPassword`).
- [x] **Paso 2**: Crear `activity_login.xml` y `LoginActivity.kt` (con login de correo/contraseña, recuperación de contraseña, Google y botón para ir a registro).
- [x] **Paso 3**: Crear `activity_register.xml` y `RegisterActivity.kt` (con formulario de datos personales y registro en Firebase Auth + Firestore).
- [x] **Paso 4**: Crear `activity_dui_verification.xml` y `DuiVerificationActivity.kt` (pantalla de escaneo/verificación de DUI estilo Uber).
- [x] **Paso 5**: Actualizar `AndroidManifest.xml` registrando las nuevas Activities y estableciendo `LoginActivity` como lanzador principal.
- [x] **Paso 6**: Limpiar código obsoleto (`RegistroAdapter`, `item_registro.xml`).
- [x] **Paso 7**: Verificar compilación exitosa (`BUILD SUCCESSFUL`).
