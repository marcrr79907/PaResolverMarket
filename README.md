# PaResolverShop 🛍️🇨🇺

**PaResolverShop** es un proyecto de aplicación de comercio electrónico moderna y multiplataforma, construida con el objetivo de ofrecer una experiencia de compra fluida y nativa tanto en Android como en iOS, utilizando una única base de código gracias a **Kotlin Multiplatform** y **Compose Multiplatform**.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7f52ff.svg?style=flat&logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4.svg?style=flat&logo=jetpackcompose)
![Firebase](https://img.shields.io/badge/Firebase-SDK-ffca28.svg?style=flat&logo=firebase)
![Koin](https://img.shields.io/badge/Koin-DI-F1873B.svg?style=flat)
![Voyager](https://img.shields.io/badge/Voyager-Navigation-B3004F.svg?style=flat)
![Status](https://img.shields.io/badge/Status-En%20Desarrollo-orange.svg?style=flat)

---

## ✨ Características Principales

- **Autenticación Completa:** Registro e inicio de sesión con Email/Contraseña y Google Sign-In.
- **Catálogo de Productos:** Visualización de productos en una lista limpia y atractiva.
- **Panel de Administración:** Sección protegida para administradores.
- **Gestión de Inventario (CRUD):**
    - **Crear:** Añadir nuevos productos con detalles e imágenes.
    - **Leer:** Ver la lista completa de productos en el inventario.
    - **Actualizar:** Editar la información de productos existentes.
    - **Eliminar:** Borrar productos de la base de datos con diálogo de confirmación.
- **Seguridad:** Las claves y secretos de la API se gestionan de forma segura a través de `local.properties`.
- **UI Reactiva:** La interfaz de usuario se actualiza en tiempo real gracias a Kotlin Flow y `StateFlow`.

---

## 🛠️ Stack Tecnológico

El proyecto sigue los principios de **Arquitectura Limpia** (UI - Domain - Data) y utiliza tecnologías de vanguardia para asegurar escalabilidad y mantenibilidad.

- **Core & UI:**
    - **[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html):** Lógica de negocio (Casos de Uso, Repositorios) compartida al 100%.
    - **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/):** UI declarativa unificada para Android e iOS.
    - **Coroutines & Flow:** Manejo de asincronía y flujos de datos reactivos.

- **Arquitectura & Navegación:**
    - **MVVM:** Patrón de diseño para la capa de UI.
    - **[Voyager](https://voyager.adriel.cafe/):** Navegación robusta y multiplatforma (Navigator, TabNavigator).
    - **[Koin](https://insert-koin.io/):** Inyección de dependencias ligera y pragmática.

- **Backend & Servicios:**
    - **Firebase Auth:** Para la autenticación de usuarios.
    - **Firebase Firestore:** Como base de datos NoSQL en tiempo real para los productos.
    - **Firebase Storage:** Para el almacenamiento y la gestión de imágenes de productos.

- **Utilidades:**
    - **[Coil3](https://coil-kt.github.io/coil/):** Carga de imágenes en Compose, con soporte KMP.
    - **[Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings):** Para persistencia de datos clave-valor de forma sencilla.

---

## 📂 Estructura del Proyecto

El código está organizado para maximizar la reutilización entre plataformas:

- **`/composeApp`**: El corazón del proyecto, contiene todos los módulos compartidos.
    - `src/commonMain`: **+95% del código.** UI (Compose), ViewModels, Casos de Uso, Repositorios, Modelos de Dominio y DTOs.
    - `src/androidMain`: Implementaciones específicas de Android (Activity, `ProductRepositoryAndroid`, `local.properties` setup).
    - `src/iosMain`: Implementaciones específicas de iOS y punto de entrada para el framework de UI.
- **`/iosApp`**: Proyecto de Xcode que consume el framework compartido de `composeApp`.

---

## 🚀 Cómo Empezar

Sigue estos pasos para configurar y ejecutar el proyecto en tu máquina local.

### Requisitos Previos
- **JDK 17** o superior.
- **Android Studio** (versión Hedgehog o más reciente).
- **Xcode 15** o superior (para ejecutar en iOS).
- El plugin de **Kotlin Multiplatform Mobile** en Android Studio.

### 1. Configuración de Firebase

1.  Crea un nuevo proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
2.  **Activa los servicios necesarios:**
    - **Authentication:** Habilita los proveedores "Email/Contraseña" y "Google".
    - **Firestore Database:** Crea una base de datos en modo de prueba.
    - **Storage:** Crea un bucket de almacenamiento.
3.  **Configura tu app de Android:**
    - Registra una nueva aplicación de Android con el package name `com.market.paresolvershop`.
    - Descarga el archivo `google-services.json` y colócalo en la carpeta `composeApp/`.
4.  **Configura tu app de iOS (Opcional):**
    - Registra una nueva aplicación de iOS.
    - Descarga el archivo `GoogleService-Info.plist` y añádelo a la raíz del proyecto en Xcode (`iosApp/`).

### 2. Clave de Cliente Web de Google

Para que el inicio de sesión con Google funcione, necesitas proporcionar tu ID de cliente web.

1.  En la **Consola de Google Cloud**, busca el ID de cliente web OAuth 2.0 que se generó para tu proyecto de Firebase.
2.  Crea un archivo llamado `local.properties` en la raíz del proyecto.
3.  Añade la siguiente línea, reemplazando `YOUR_WEB_CLIENT_ID` con tu clave:
    ```properties
    web_client_id=YOUR_WEB_CLIENT_ID
    ```

### 3. Ejecutar la Aplicación

- **🤖 Android:**
    1.  Abre el proyecto en Android Studio.
    2.  Espera a que Gradle se sincronice.
    3.  Selecciona `composeApp` en la configuración de ejecución y elige un emulador o dispositivo físico.
    4.  ¡Haz clic en "Run"!

- **🍏 iOS:**
    1.  Abre el archivo `iosApp/iosApp.xcworkspace` en Xcode.
    2.  Elige un simulador o un dispositivo físico.
    3.  ¡Haz clic en "Run"!

---

## 📄 Licencia

Este proyecto está distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
