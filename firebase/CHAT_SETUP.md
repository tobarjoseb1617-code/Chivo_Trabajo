# Chat en tiempo real

El chat usa la configuración Firebase existente de `app/google-services.json` y la sesión local de `AuthRepository` (`chivo_credenciales_prefs`), igual que el inicio de sesión actual. El UID del chat, sus remitentes y el historial salen de esa misma sesión. Al abrir una tarjeta de un profesional se crea (si todavía no existe) una conversación para ambos UID; no hace falta crear colecciones vacías ni datos de ejemplo.

- `conversaciones/{hashDeLosDosUid}`: `participantes` (dos UID), `nombres` (mapa UID/nombre), `ultimoMensaje`, `actualizadoEn` (hora del servidor).
- `conversaciones/{id}/mensajes/{idAutomatico}`: `remitenteUid`, `tipo` (`texto`, `imagen`, `video`, `audio`), `texto`, `archivoUrl`, `mimeType`, `creadoEn` (hora del servidor).

Inicio muestra todos los documentos de `profesionales`, sin filtrar por ciudad, distancia, disponibilidad, estado de cuenta ni usuario actual. Se respeta el campo `uid` del profesional (o el ID del documento si está vacío). Nombre y foto se leen del propio documento si existen y se completan opcionalmente desde `Usuario/{uid}`; si ese perfil falta o su lectura falla, la tarjeta sigue visible. Un documento sin nombre muestra «Profesional sin nombre registrado». La disponibilidad y las reseñas se muestran según sus datos reales. La lista escucha cambios de `profesionales` mientras Inicio está visible. Si Firestore rechaza la lectura de esa colección, se muestra un mensaje específico de permisos.

## Reglas Firebase

El archivo `firestore.chat.rules` presupone usuarios autenticados mediante **Firebase Auth** y `request.auth.uid`. La autenticación local actual no genera esa identidad: este fragmento no se puede aplicar tal cual a la sesión local. Para imponer permisos por participante en el servidor se debe integrar autenticación verificable (Firebase Auth con UID coincidente, o un backend que emita custom tokens). No abrir reglas generales para resolver un error de permisos.

Para una instalación con Firebase Auth y UID coincidente, integrar el bloque de `firestore.chat.rules` **dentro de** `match /databases/{database}/documents` en las reglas actuales de Firestore y publicarlas en Firebase Console. Este archivo es un fragmento, no un reemplazo completo de las reglas de las otras colecciones. Conservar los permisos que usa la aplicación para consultar perfiles `Usuario` y `profesionales`.

Las reglas permiten leer conversaciones solo a sus participantes y crear mensajes únicamente como el usuario autenticado. Los participantes no pueden cambiarse. El envío del mensaje y el resumen de la conversación se guardan en un lote atómico. La consulta del historial usa `whereArrayContains` sin un índice compuesto adicional.

Revisar si existen reglas generales como `match /{document=**} { allow read, write: if true; }`: los permisos de las reglas coincidentes se suman y una regla general permisiva anularía la privacidad de estas restricciones. No desplegar un archivo nuevo que borre las reglas actuales.

Este repositorio no tenía reglas ni una sesión administrativa de Firebase disponible para publicarlas; el fragmento queda preparado para su integración. Las colecciones se crean cuando la aplicación guarda la primera conversación/mensaje con permisos publicados.

## Multimedia Cloudinary

El módulo lee `CLOUDINARY_CLOUD_NAME` y `CLOUDINARY_UPLOAD_PRESET` desde `local.properties` mediante los campos BuildConfig existentes. El preset debe permitir cargas **unsigned**, fotos, videos y audio. No se empaqueta API Secret para firmar peticiones en el dispositivo. La documentación de [presets de Cloudinary](https://cloudinary.com/documentation/upload_presets) describe esa configuración.

Los archivos se transmiten directamente a Cloudinary y en Firestore solo se guarda la URL HTTPS y su tipo. Los audios usan el recurso `video` de Cloudinary. Máximo por archivo: 50 MB. Las notas de voz duran hasta dos minutos; el permiso de micrófono se solicita al grabar. Al salir del chat se descarta una grabación sin enviar. Si falla el envío de un archivo se muestra un botón para reintentarlo. Las fotos y videos se eligen con el selector de documentos de Android, sin permiso general de almacenamiento.

La reproducción abre un visor dentro de la aplicación. Cloudinary entrega estas URLs como recursos públicos: las reglas de Firestore protegen el acceso a los mensajes, pero no bloquean una URL multimedia ya compartida. Para entrega autenticada se necesita un backend que firme el acceso.

## Verificación con dos cuentas

1. Iniciar sesión con un cliente y tener un profesional real disponible en `profesionales`, con su perfil correspondiente en `Usuario`.
2. Abrir **Inicio → Profesionales cerca de ti → Abrir chat**. Verificar que la cabecera muestra el nombre real.
3. Con la cuenta del profesional abrir **Mensajes** en otro dispositivo. La conversación debe aparecer sin recargar.
4. Enviar texto en ambos sentidos y comprobar que llega en vivo, sin duplicarse al volver a abrir el contacto.
5. Adjuntar una foto, video y audio; reproducirlos desde ambos dispositivos. Grabar y enviar una nota de voz.
6. Denegar el micrófono, intentar un archivo mayor a 50 MB, interrumpir la red y probar reenvío. Comprobar el mensaje de error y que el texto no se borra si el envío falla.
7. Con una tercera cuenta verificar que las reglas publicadas rechazan leer o escribir en la conversación ajena y que rechazan suplantar al remitente.
