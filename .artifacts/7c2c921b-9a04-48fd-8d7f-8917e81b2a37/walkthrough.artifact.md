# Walkthrough - Diálogos Modales para Portafolio y Restricción de Imágenes

Se han implementado diálogos modales rápidos y limpios en `PortafolioProfesionalActivity` para "+ Agregar trabajo" y "+ Agregar título / Acreditación", limitando estrictamente la selección a archivos de imagen (`image/*`) y guardando los datos en la subcolección `Portafolio` de Firestore.

## Changes Made

### 1. Diálogos Modales Interactivos (`PortafolioProfesionalActivity.kt`)
- **Agregar Trabajo**: Abre un diálogo modal para seleccionar una imagen de trabajo (`image/*`), ingresar el título y la descripción, subiéndola a Cloudinary y guardándola en Firestore.
- **Agregar Título / Acreditación**: Abre un diálogo modal para seleccionar una imagen de carnet/título (`image/*`) y su respectivo título.

### 2. Restricción Estricta de Imágenes (`CrearPortafolioActivity.kt`)
- Se restringieron todos los selectores de archivos a `image/*`, evitando la selección de documentos genéricos o PDFs.

---

## Verification Results

### Automated Tests
- Compilación del proyecto:
  ```bash
  > Task :app:assembleDebug
  BUILD SUCCESSFUL in 10s
  ```
- El proyecto compila limpiamente sin errores.
