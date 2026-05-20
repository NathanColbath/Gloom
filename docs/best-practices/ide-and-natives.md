# IDE & Natives

LWJGL loads native libraries (GLFW, OpenGL, OpenAL, STB) from platform-specific JAR classifiers. LLW depends on these through the `:llw` Gradle module.

## Gradle run (recommended)

```bash
./gradlew run          # Unix
gradlew.bat run        # Windows
```

The Gloom root build applies JVM args for native extraction automatically.

## IntelliJ IDEA / VS Code

Add VM options to your run configuration:

```
-Dorg.lwjgl.system.SharedLibraryExtractPath=build/lwjgl-natives
```

Create `build/lwjgl-natives` or point to any writable directory. LWJGL extracts `.dll` / `.so` / `.dylib` on first load.

::: tip Module classpath
Run the **application** module (`src/main/java`), not `:llw` alone. The app module must depend on `implementation(project(":llw"))` so natives resolve.
:::

## Classpath sanity

Required at runtime:

- `lwjgl.jar` + `lwjgl-glfw`, `lwjgl-opengl`, `lwjgl-openal`, `lwjgl-stb`
- Matching `natives-windows` (or macos/linux) classifiers

Copy the `dependencies` block from `llw/build.gradle.kts` if you consume LLW as a plain JAR without Gradle's platform dependency management.

## Working directory

Filesystem loads (`Path.of("assets/...")`) resolve relative to the JVM working directory — usually the project root when using Gradle, sometimes `llw/` when misconfigured in IDE.

Prefer classpath resources for shipped assets; use filesystem paths for dev tools and mods.

::: warning Multiple extractions
Different extract paths per run configuration can leave stale natives. One writable path per project is enough.
:::

## Debugging native load failures

1. Run with `-Dorg.lwjgl.util.Debug=true` for verbose loader output.
2. Confirm JDK **17+** (LLW bytecode target).
3. On Apple Silicon, use `natives-macos-arm64`, not x64 Rosetta natives.

## See also

- [Project Setup](/guide/project-setup)
- [FAQ](/faq)
- [Getting Started](/guide/getting-started)
