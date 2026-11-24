# Mi Launcher (Android)

Launcher con barras personalizables: colores automáticos según el icono de cada app, 38 decoraciones,
brillo (constante, al azar, secuencia, ola…), destellos al tocar, sonidos, fuentes, formas de icono,
imagen de galería, temas, búsqueda y animaciones de entrada.

Requiere Android 8.0 (API 26) o superior.

## Cómo obtener el APK (con GitHub, sin instalar nada)

1. Crea un repositorio nuevo en GitHub (puede ser privado).
2. Sube **todo el contenido** de esta carpeta, incluida la carpeta oculta `.github`.
   - Lo más fiable: `git` o GitHub Desktop.
   - Si usas la web y `.github` no se sube al arrastrar: **Add file → Create new file**, escribe
     `.github/workflows/build.yml` y pega el contenido del archivo del mismo nombre.
3. Abre la pestaña **Actions**. La compilación arranca sola con cada subida (o pulsa **Run workflow**).
4. Cuando termine (unos 5–10 min la primera vez), entra en la ejecución y descarga **MiLauncher-apk**
   en la sección *Artifacts*. Es un .zip que contiene el .apk.
5. Pasa el .apk al teléfono, ábrelo y permite "instalar apps desconocidas".
6. Pulsa el botón Inicio y elige **Mi Launcher** → "Siempre".

Para volver a tu launcher anterior: Ajustes → Apps → Apps predeterminadas → App de inicio.

## Uso

- Toca una barra: se abre la app (con destellos y sonido si están activados).
- Mantén presionada una barra: panel para cambiar barra, decoración, brillo, texto, icono y sonido.
- Botón ⚙ (arriba a la derecha): sonido/brillo global, velocidad de la secuencia, temas, animación de entrada.
- "Copiar estilo a todas" aplica el look de una barra al resto.

## Fuentes

El sistema trae 12 fuentes. Para añadir más:
- Copia archivos `.ttf` / `.otf` (por ejemplo de fonts.google.com) en `app/src/main/assets/fonts/` y vuelve a compilar; o
- Desde la app: pestaña **Texto → Importar fuente**.

## Optimización incluida

- Compilación *release* con R8 (reduce y optimiza el código) y eliminación de recursos sin usar.
- Lista perezosa (solo se dibujan las barras visibles) con claves estables.
- El reloj de animación solo pide frames cuando algo se anima; en reposo no gasta batería.
- Las formas decorativas se calculan una vez y se reutilizan; el brillo se evalúa en la fase de dibujo.
- Iconos reducidos y cargados fuera del hilo principal; guardado con retardo para no escribir en cada gesto.

## Si la compilación falla

Abre la ejecución fallida en **Actions**, copia el mensaje de error de la sección roja y envíaselo
a Claude para que lo corrija.
