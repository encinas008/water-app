# Water App - Registro de Consumo

Aplicación móvil nativa para el registro de consumo de agua, diseñada para integrarse con la API de servicios de agua.

## 🛠 Tecnologías Utilizadas

- **Lenguaje:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Networking:** Retrofit 2.11.0 & OkHttp 4.12.0
- **Asincronía:** Kotlin Coroutines & Flow
- **Arquitectura:** MVVM (Model-View-ViewModel)

## 📋 Requisitos del Entorno

Para compilar y ejecutar este proyecto, necesitas:

- **Java JDK:** 17 (Requerido por Android Gradle Plugin 8.13.2)
- **Gradle:** 8.13
- **Android Studio:** Ladybug o superior recomendado

> [!IMPORTANT]
> Si encuentras errores de compilación relacionados con la versión de Java, asegúrate de que tu `JAVA_HOME` apunte a la versión 17.
> ```bash
> export JAVA_HOME=/path/to/java-17
> ```

## 🌐 Configuración de Entornos

La aplicación utiliza **Build Variants** para manejar diferentes servidores automáticamente:

| Variante | Propósito | URL de la API (Default) |
| :--- | :--- | :--- |
| **Debug** | Desarrollo / Emulador | `http://10.0.2.2:8085/api/` |
| **Release** | Producción | Configurable en `build.gradle.kts` |

## 🚀 Comandos de Construcción

Desde la raíz del proyecto (`water-app`):

### Generar APK de Desarrollo
```bash
./gradlew assembleDebug
```
*El APK se encontrará en: `app/build/outputs/apk/debug/app-debug.apk`*

### Generar APK de Producción (Testable)
```bash
./gradlew assembleRelease
```
*El APK se encontrará en: `app/build/outputs/apk/release/app-release.apk`. Este APK ya está firmado con la llave de debug para que puedas instalarlo directamente en tu celular sin errores de "paquete inválido".*

## 🎨 Diseño y Estética
El diseño está inspirado en **water-ui**, utilizando una paleta de colores profesional:
- **Primary:** `#3B66FF` (Vibrant Blue)
- **Background:** `#F8FAFC` (Ghost White)
- **Contrast:** Slate grey para textos secundarios.
