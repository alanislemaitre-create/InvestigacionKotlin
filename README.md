# TiendaApp — Kotlin Multiplatform + Compose Multiplatform

Aplicación de catálogo de productos hecha con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**. Un solo código Kotlin, ubicado en `shared/commonMain`, se ejecuta en **Android** y en **escritorio (JVM)**. Los datos se consumen de una **API REST en Django** mediante el cliente HTTP **Ktor**.

Proyecto de la investigación de frameworks móviles del curso **Desarrollo de Software IV**, Bachillerato en Informática Empresarial, Universidad de Costa Rica, Sede Regional del Pacífico.

**Integrantes:** [Integrante 1] · [Integrante 2] · [Integrante 3]

---

## Funcionalidades

| Requisito | Cómo se cumple |
| --- | --- |
| Proyecto ejecutable | Corre en escritorio (JVM) y en el emulador de Android |
| Dos pantallas con navegación | `ListadoScreen` (catálogo) y `FormularioScreen` (crear o editar); la navegación se controla con un estado (`enum Screen`) |
| Formulario con validación | Nombre, descripción, precio, stock y un switch *Activo*. Valida campos obligatorios, precio mayor a 0 y stock de 0 o más; muestra los errores en cada campo y un mensaje de éxito |
| Listado dinámico | `LazyColumn` de tarjetas con los productos que entrega la API |
| Consumo de API REST | CRUD completo (GET, POST, PUT, DELETE) contra la API Django desarrollada en el curso |

---

## Requisitos

### Aplicación móvil y de escritorio (`TiendaApp`)

| Herramienta | Versión usada |
| --- | --- |
| IntelliJ IDEA o Android Studio, con el plugin **Kotlin Multiplatform** | Versión estable reciente |
| JDK | 17 o superior (viene con Android Studio) |
| Gradle | 9.5.1 (se descarga solo con `gradlew`) |
| Android SDK | compileSdk 37, minSdk 24 |
| Emulador de Android o teléfono físico | Solo para la versión Android |

Versiones principales de las librerías (ver `gradle/libs.versions.toml`):

| Librería | Versión |
| --- | --- |
| Kotlin | 2.4.21 |
| Compose Multiplatform | 1.12.2 |
| Ktor Client | 3.1.3 |
| kotlinx.serialization | 1.8.1 |
| Lifecycle ViewModel (JetBrains) | 2.11.0 |
| Android Gradle Plugin | 9.1.1 |

> Para compilar la versión iOS haría falta una Mac con Xcode. Este proyecto no incluye el destino iOS.

### API (`backend-tienda-django`)

| Herramienta | Versión usada |
| --- | --- |
| Python | 3.12 o superior |
| Django | 6.1.3 |
| Django REST Framework | 3.18.3 |

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone [URL del repositorio]
cd [nombre del repositorio]
```

### 2. Levantar la API Django

```bash
cd backend-tienda-django

# Crear y activar el entorno virtual
python -m venv venv
venv\Scripts\activate          # Windows
# source venv/bin/activate     # macOS / Linux

# Instalar dependencias y crear la base de datos
pip install -r requirements.txt
python manage.py migrate

# Cargar los datos de prueba (7 productos y 2 categorías)
python manage.py loaddata ../datos_prueba/productos.json

# Iniciar el servidor
python manage.py runserver 0.0.0.0:8000
```

Para comprobar que funciona, abra http://127.0.0.1:8000/api/productos/ en el navegador. Debe ver la lista de productos en JSON.

### 3. Ejecutar la aplicación

Abra la carpeta `TiendaApp` en IntelliJ IDEA o Android Studio y espere a que Gradle termine de sincronizar. Puede ejecutarla desde las configuraciones del IDE o desde la terminal:

```bash
cd TiendaApp

# Escritorio (JVM)
.\gradlew :desktopApp:run           # Windows
./gradlew :desktopApp:run           # macOS / Linux

# Escritorio con recarga en caliente
./gradlew :desktopApp:hotRun --auto

# Android: con el emulador ya abierto, compila e instala la app
./gradlew :androidApp:installDebug

# Android: solo generar el APK
./gradlew :androidApp:assembleDebug
```

### 4. Configuración para ejecutar en Android

En el emulador de Android, `127.0.0.1` apunta al propio emulador, no a la computadora. Para que la app llegue a la API se necesitan tres ajustes:

1. **URL de la API.** En el emulador se usa `http://10.0.2.2:8000`. En escritorio se usa `http://127.0.0.1:8000`. La URL se define en `shared/src/commonMain/.../network/ApiClient.kt`.
2. **Permisos en `androidApp/src/main/AndroidManifest.xml`:**
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <application android:usesCleartextTraffic="true" ...>
   ```
   El segundo atributo permite tráfico `http://` sin cifrar, que Android bloquea por defecto. Úselo solo en desarrollo.
3. **Django:** en `backend_tienda/settings.py` agregue la dirección del emulador:
   ```python
   ALLOWED_HOSTS = ['127.0.0.1', 'localhost', '10.0.2.2']
   ```

---

## Endpoints utilizados

| Método | Endpoint | Acción | Respuesta esperada |
| --- | --- | --- | --- |
| GET | `/api/productos/` | Listar productos | 200 |
| POST | `/api/productos/` | Crear producto | 201 |
| PUT | `/api/productos/{id}/` | Actualizar producto | 200 |
| DELETE | `/api/productos/{id}/` | Eliminar producto | 204 |

La API también valida los datos: rechaza un precio de 0 o menos y un stock negativo (respuesta 400).

---

## Estructura del proyecto

```
.
├── README.md
├── datos_prueba/
│   └── productos.json              # Datos de prueba para la API (sin usuarios ni credenciales)
├── backend-tienda-django/          # API REST (Django + DRF)
│   ├── requirements.txt
│   ├── manage.py
│   ├── backend_tienda/             # Configuración: settings.py, urls.py
│   └── productos/                  # Modelos, serializadores, vistas y rutas
└── TiendaApp/                      # Aplicación Kotlin Multiplatform
    ├── settings.gradle.kts         # Declara los módulos shared, androidApp y desktopApp
    ├── gradle/libs.versions.toml   # Catálogo de versiones
    ├── shared/                     # Librería KMP con todo el código común
    │   └── src/
    │       ├── commonMain/kotlin/org/example/tiendaapp/
    │       │   ├── App.kt                         # Composable raíz y navegación
    │       │   ├── model/Producto.kt              # Modelo @Serializable
    │       │   ├── network/ApiClient.kt           # Cliente HTTP con Ktor (CRUD)
    │       │   ├── viewmodel/ProductoViewModel.kt # Estado (StateFlow) y validaciones
    │       │   └── ui/
    │       │       ├── ListadoScreen.kt           # Pantalla 1: catálogo
    │       │       └── FormularioScreen.kt        # Pantalla 2: crear / editar
    │       ├── androidMain/                       # Implementación actual para Android
    │       └── jvmMain/                           # Implementación actual para escritorio
    ├── androidApp/                 # Punto de entrada Android: MainActivity.kt
    └── desktopApp/                 # Punto de entrada escritorio: main.kt
```

### Cómo fluye la aplicación

1. **Punto de entrada.** `MainActivity` (Android) y `main()` (escritorio) llaman al mismo composable `App()` de `commonMain`.
2. **Navegación.** `App()` guarda la pantalla actual en un estado (`Screen.LISTADO` o `Screen.FORMULARIO`) y muestra la que corresponde.
3. **Estado.** `ProductoViewModel` expone dos `StateFlow`, uno para el listado y otro para el formulario. Las pantallas los observan con `collectAsState()` y se redibujan cuando cambian.
4. **Eventos.** Cada campo del formulario llama una función del ViewModel (`updateNombre`, `updatePrecio`, etc.). Al guardar, `validarFormulario()` revisa los datos antes de enviarlos.
5. **Red.** `ApiClient` usa Ktor con `ContentNegotiation` y kotlinx.serialization para convertir el JSON en objetos `Producto`. Como Django envía el precio como texto, `DoubleOrStringSerializer` lo convierte a número.

---

## Limitaciones

- **Sin versión iOS.** El proyecto no incluye el destino iOS, porque compilarlo exige una Mac con Xcode.
- **URL fija.** La dirección de la API está escrita en `ApiClient.kt`. Para cambiar entre escritorio y Android hay que modificarla, o definirla por plataforma con `expect`/`actual`.
- **Solo HTTP local.** La app espera la API en la misma computadora y sin HTTPS. No está preparada para un servidor en producción.
- **Sin autenticación.** Los endpoints de productos que usa la app son públicos; la app no maneja inicio de sesión ni tokens.
- **Sin almacenamiento local.** Si la API no responde, la app muestra un mensaje de error con el botón *Reintentar*, pero no guarda datos sin conexión.
- **Navegación simple.** Se usa un `enum` en lugar de la librería Navigation, suficiente para dos pantallas pero sin historial ni *deep links*.
- **Apariencia Material.** Compose dibuja su propia interfaz, por lo que la app se ve igual en todas las plataformas en lugar de usar los controles nativos de cada sistema.

---

## Seguridad

- El repositorio **no incluye** la base de datos (`db.sqlite3`) ni usuarios, contraseñas o tokens. Los datos de prueba están en `datos_prueba/productos.json` y solo contienen productos y categorías.
- La `SECRET_KEY` de `settings.py` es una clave de desarrollo y no debe usarse en producción.

---

## Referencias

- JetBrains. (s.f.). *Kotlin Multiplatform*. https://kotlinlang.org/docs/multiplatform/get-started.html
- JetBrains. (s.f.). *Compose Multiplatform*. https://kotlinlang.org/docs/multiplatform/compose-multiplatform.html
- Ktor. (s.f.). *Client engines*. https://ktor.io/docs/client-engines.html
- Django REST Framework. (s.f.). *Home*. https://www.django-rest-framework.org/
