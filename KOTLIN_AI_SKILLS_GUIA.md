# Guía y Uso de Kotlin AI Skills en ChivoTrabajo

Este documento explica qué son las **Kotlin AI Skills** (`Kotlin/kotlin-agent-skills`), cómo están estructuradas en este proyecto, las ventajas que proporcionan y **cómo se deben utilizar en este chat cada vez que se vaya a modificar código**.

---

## 🤖 1. ¿Qué son las Kotlin AI Skills?
Las **Kotlin AI Skills** son módulos de conocimiento estandarizados y portables creados por el equipo de Kotlin (siguiendo el estándar abierto `agentskills.io`). Proporcionan a los asistentes y agentes de Inteligencia Artificial contexto especializado, reglas de arquitectura, patrones idiomáticos y flujos de trabajo optimizados para el desarrollo en **Kotlin** y **Kotlin Multiplatform (KMP)**.

---

## 🏛️ 2. ¿Cómo están estructuradas?
Las skills se almacenan localmente en el repositorio bajo el directorio `.skills/` y siguen una estructura estandarizada:
* **Metadatos (`skill.json` / `SKILL.md`)**: Definen el nombre, descripción y el conjunto de instrucciones técnicas que el agente debe seguir rigurosamente al ejecutar una tarea específica.
* **Guías de Flujo**: Instrucciones paso a paso para evitar errores comunes (por ejemplo, migración de dependencias, gestión asíncrona avanzada, patrones MVVM, etc.).

---

## 🚀 3. Ventajas que Proporcionan
1. **Reducción de Errores (Grounding)**: Evitan que el modelo alucine o utilice sintaxis obsoletas, asegurando código Kotlin idiomático y actualizado.
2. **Consistencia Arquitectónica**: Garantizan que la estructura del proyecto (MVVM, Corrutinas, Repositories, ViewModels) se mantenga intacta y limpia.
3. **Optimización de Tareas Complejas**: Guían al agente a través de operaciones multi-paso complejas con precisión milimétrica.

---

## 💬 4. Protocolo de Uso en este Chat (¡Muy Importante!)

A partir de ahora, **cada vez que vayamos a modificar, refactorizar o crear código en este chat**, seguiremos estrictamente este protocolo:

1. **Activación de la Skill**: Antes de aplicar cualquier cambio en un archivo, anunciaré explícitamente la skill correspondiente que se está aplicando (por ejemplo: `[Kotlin AI Skill: Kotlin Idiomatic Best Practices & MVVM Architecture]`).
2. **Aplicación Guiada**: El código se generará utilizando exclusivamente los patrones validados por la skill correspondiente (manejo de hilos con Corrutinas `Dispatchers.IO`/`Dispatchers.Main`, ViewBinding seguro, manejo de errores con `Result<T>`, etc.).
3. **Verificación y Build**: Después de aplicar la skill y modificar el código, se ejecutará automáticamente la verificación de compilación (`gradle build`) para garantizar que todo funcione al 100%.

---

### 📥 ¿Cómo se añadirían nuevas skills en el futuro mediante CLI?
Si en algún momento deseas añadir nuevas habilidades desde la terminal utilizando el ecosistema de skills, puedes ejecutar:
```shell
npx skills add Kotlin/kotlin-agent-skills
```
Y seleccionar la skill de tu preferencia con alcance a nivel de proyecto (`Project`).
