# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a freshly generated Android Studio "Empty Activity" (Jetpack Compose) project. There is a single module (`app`) and no custom application code beyond the default template (`MainActivity.kt` with a `Greeting` composable, and the generated Material3 theme files under `ui/theme/`). Treat this as a blank slate rather than an established codebase with conventions to preserve.

## Commands

Build and test from the project root using the Gradle wrapper (`gradlew.bat` on Windows, `gradlew` on Unix shells).

- Build debug APK: `gradlew.bat assembleDebug`
- Full build (compile + lint + unit tests): `gradlew.bat build`
- Run unit tests (JVM, in `app/src/test`): `gradlew.bat testDebugUnitTest`
- Run a single unit test: `gradlew.bat testDebugUnitTest --tests "com.example.lionideaton.ExampleUnitTest.methodName"`
- Run instrumented tests (device/emulator required, in `app/src/androidTest`): `gradlew.bat connectedDebugAndroidTest`
- Lint: `gradlew.bat lint`
- Clean: `gradlew.bat clean`

## Architecture

- **Package/namespace**: `com.example.lionideaton`, `minSdk 24`, `targetSdk`/`compileSdk 34`.
- **UI toolkit**: Jetpack Compose (no XML layouts, no Fragments). `compileOptions`/`kotlinOptions` target Java 8.
- **Entry point**: `MainActivity` (`ComponentActivity`) calls `setContent` directly with composables — there is no navigation graph or dependency-injection framework set up yet.
- **Theming**: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` follow the standard Compose Material3 theme scaffold (`LionideatonTheme` wrapper).
- **Dependency versions**: managed centrally via the Gradle version catalog at `gradle/libs.versions.toml` (referenced in build files as `libs.*`). Add new dependencies there rather than hardcoding coordinates in `app/build.gradle.kts`.
- **Tests**: `app/src/test` for local JVM unit tests, `app/src/androidTest` for instrumented (on-device) tests — both currently contain only the generated example test.
