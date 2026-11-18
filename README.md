# Barista de bolsillo

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg)](https://developer.android.com/about/versions/nougat)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-yellow.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**Barista de bolsillo** (antes *Barista App*) es una aplicación nativa para Android pensada para ayudar a los amantes del café a lograr la taza perfecta. Calcula la cantidad exacta de café según el agua y la intensidad elegidas, recomienda la molienda ideal para cada método, incluye un cronómetro de extracción y permite guardar, compartir y puntuar recetas con una comunidad de usuarios.

---

## Características principales

### Preparación guiada (Inicio)
- **Métodos de preparación:** grilla de 6 tarjetas con foto (Prensa Francesa, Moka, Filtro / Jarra, V60, Chemex y Expreso Hogareño). Los métodos se cargan desde Firestore y cada uno tiene su pantalla de detalle.
- **Flujo en 4 pasos:**
  1. Elegir el método de preparación.
  2. Elegir cantidad de agua e intensidad (Suave, Media, Fuerte).
  3. Elegir el tipo de molienda. Si se elige **"En grano"** cuando el método necesita molienda, la app muestra una advertencia y **bloquea el botón Continuar** hasta corregirlo.
  4. Ver la receta calculada, con cronómetro libre y opción de guardarla.
- **Calculadora de proporciones:** determina los gramos de café a partir del agua (ml) y la intensidad.
- **Recomendación de molienda:** indica el grosor ideal según el método.
- **Tu actividad y Recientes:** tira de estadísticas (recetas, favoritos, puntuación) y las últimas recetas creadas, directamente en el Inicio.
- **Calculadora y Temporizador:** pantalla propia, accesible al final del scroll del Inicio, con botón para volver.

### Recetas
- **Guardar recetas** propias en Firestore, con **foto opcional** (se comprime y se sube a Cloudinary).
- **Detalle de receta:** ver datos, editar la nota, marcar como favorita, eliminar, publicar en la comunidad y compartir.
- **Compartir por WhatsApp:** el texto sale con la firma *"Creado con Barista de bolsillo"*.

### Comunidad
- **Compartidos:** feed con las recetas publicadas por todos los usuarios.
- **Puntuación con estrellas:** cada usuario vota una vez por receta (se puede cambiar el voto). El promedio se calcula con transacciones de Firestore para evitar inconsistencias.
- **Favoritos:** lista con las recetas propias y de la comunidad marcadas como favoritas.

### Cuenta y perfil (Yo)
- **Autenticación con Firebase Auth:** registro (con nombre de usuario único y confirmación de contraseña), inicio y cierre de sesión.
- **Perfil:** cabecera con avatar y datos de la cuenta, estadísticas, lista de tus recetas con confirmación antes de borrar, interruptor de **modo oscuro** (se recuerda entre sesiones) y cierre de sesión.

---

## Rediseño de interfaz ("Espresso & Sage")

Toda la app se rediseñó con un sistema visual propio y coherente:

- **Paleta clara y oscura:** verde musgo como acento, grises neutros y cabecera verde oscuro. Definida con tokens `ds_*` en `colors.xml` y mapeada a los atributos del tema Material 3 (`themes.xml` y `values-night/themes.xml`).
- **Tipografías:** *Bricolage Grotesque* para títulos y nombres, e *Instrument Sans* para el cuerpo (en regular, medium y semibold). Los estilos `Text.Barista.*` las aplican de forma centralizada.
- **Componentes reutilizables:** estilos `Widget.Barista.*` (botones primario, secundario y de peligro, tarjetas, campos de texto, chips de selección) y formas con esquinas redondeadas.
- **Cabecera y hoja:** cabecera verde oscuro con logo y avatar, y una hoja blanca de esquinas redondeadas que se superpone.
- **Barra de navegación flotante:** píldora con 4 pestañas (Inicio, Compartidos, Favoritos, Yo). La pestaña activa se expande y muestra su nombre, con animación.
- **Iconografía SVG:** íconos vectoriales (`VectorDrawable`) para navegación, campos y logo.
- **Logo e ícono de la app:** el grano de café como logo, ícono adaptativo del launcher y nombre de la app "Barista de bolsillo".
- **Pantalla de Login:** foto de fondo a pantalla, titular *"Tu café, a tu medida."* y formulario sobre una hoja redondeada.
- **Animaciones:** transición de la barra de navegación entre pestañas.

---

## Arquitectura y tecnologías

| Área | Tecnología |
|------|------------|
| Lenguaje | Java |
| UI | Material Components 3, ConstraintLayout, RecyclerView, Fragments, CardView |
| Autenticación | Firebase Authentication |
| Base de datos | Cloud Firestore |
| Imágenes | Glide (con imagen de reemplazo) + subida a Cloudinary |
| Navegación | `NavigationRouter` con patrón Strategy |
| Preferencias | `SharedPreferences` (modo oscuro) |
| SDK | minSdk 24, targetSdk 37 |

### Estructura del proyecto

```
app/src/main/java/com/example/barista_app/
├── MainActivity.java            # Contenedor, cabecera y barra de navegación flotante
├── LoginActivity.java           # Inicio de sesión y registro
├── MethodDetailActivity.java    # Detalle de un método de preparación
├── RecipeDetailActivity.java    # Detalle, edición y publicación de una receta
├── NavigationRouter.java        # Enrutador de pestañas
├── ImageLoader.java / UiUtils.java
├── strategy/                    # Estrategias de navegación (Home, Share, Favorite, Me)
├── fragments/                   # Home, CoffeeOrder, Share, IdealRecipes (Favoritos), Profile, TimerCalculator
├── components/                  # TimerComponent, CalculatorComponent, ButtonCalcTimer
├── adapters/                    # RecipeAdapter, ProfileRecipeAdapter
├── models/                      # Recipe, CoffeeMethod
└── data/                        # RecipeRepository, MethodRepository, MethodImages,
                                 # ImageUploader, CloudinaryConfig
```

### Colecciones de Firestore
- `coffee_methods`: métodos de preparación.
- `recipes`: recetas (con subcolección `ratings` para los votos y campos `ratingSum` y `ratingCount`).
- `users`: datos del usuario, con subcolección `favorites`.

---

## Instalación y configuración

1. Clona el repositorio y ábrelo con **Android Studio**.
2. Verifica que exista `app/google-services.json` (configuración de Firebase).
3. En `local.properties` (raíz del proyecto, no se sube al repositorio) agrega las credenciales de Cloudinary:
   ```properties
   cloud.name=TU_CLOUD_NAME
   upload.preset=TU_UPLOAD_PRESET
   ```
4. Sincroniza Gradle y ejecuta en un emulador o dispositivo con Android 7.0 (API 24) o superior.

> Se necesita conexión a Internet: la app usa Firebase y carga las imágenes de los métodos desde la web.

---

## Licencia

Distribuido bajo licencia MIT. Consulta el archivo `LICENSE` para más información.
