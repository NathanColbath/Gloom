# Project Setup

## In this section

| Page | Description |
|------|-------------|
| [Getting Started](/guide/getting-started) | Run the demo, requirements |
| [Coordinates](/guide/coordinates) | Frame loop and camera |
| [Best Practices — IDE & Natives](/best-practices/ide-and-natives) | IntelliJ vs Gradle |
| [Javadoc](/api/javadoc) | API reference generation |

## Multi-module Gradle (recommended)

The Gloom repository is structured as:

```
Gloom/
  llw/          ← library (group org.llw)
  src/          ← demo app (depends on :llw)
```

Root [`settings.gradle.kts`](https://github.com) includes the subproject:

```kotlin
rootProject.name = "Gloom"
include("llw")
```

Your application module depends on the library:

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation(project(":llw"))
}

application {
    mainClass = "com.example.MyGame"
}
```

The `:llw` module owns all LWJGL dependencies and native classifiers. Your app module stays thin.

## Using LLW as a local JAR

Build the library:

```bash
./gradlew :llw:jar
```

The JAR is at `llw/build/libs/llw-1.0-SNAPSHOT.jar`. You still need LWJGL on the runtime classpath with matching natives — copying the dependency block from `llw/build.gradle.kts` is the safest approach.

## Package overview

| Old name (pre-LLW) | New package |
|--------------------|-------------|
| `org.gloom.renderbackend` | `org.llw.render` |
| `org.gloom.audiobackend` | `org.llw.audio` |
| `org.gloom.math` | `org.llw.math` |

Subpackages are preserved: `org.llw.render.graphics`, `org.llw.render.window`, `org.llw.math.vector`, etc.

## Classpath resources

Bundled assets live under `llw/src/main/resources/llw/`:

| Path | Contents |
|------|----------|
| `llw/render/fonts/` | Default fonts |
| `llw/audio/samples/` | Demo WAV/OGG files |

Load them with classpath-relative strings:

```java
Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 28);
audio.loadSoundBuffer("llw/audio/samples/click.wav");
```

## See also

- [Getting Started](/guide/getting-started)
- [Render Overview](/render/overview)
- [Javadoc](/api/javadoc)
