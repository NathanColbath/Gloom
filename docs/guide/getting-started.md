# Getting Started

![LLW Studio — LWJGL Lightweight Wrapper](/llw-studio-logo.png)

LLW (Lightweight LWJGL Wrapper) is a Java 17 library built on LWJGL 3. It provides three modules:

| Module | Package | Purpose |
|--------|---------|---------|
| Render | `org.llw.render` | 2D OpenGL rendering |
| Audio | `org.llw.audio` | OpenAL sounds and music |
| Math | `org.llw.math` | 2D vectors, geometry, collision |

::: tip New to LLW?
Start the [10-chapter tutorial track](/tutorials/01-window) for a guided path from zero to a game loop.
:::

## In this section

| Page | Description |
|------|-------------|
| [Tutorial 1 — Window](/tutorials/01-window) | Recommended first step |
| [Project Setup](/guide/project-setup) | Gradle multi-module setup |
| [Coordinates](/guide/coordinates) | Y-down, frame loop, screen ↔ world |
| [FAQ](/faq) | Troubleshooting |
| [SFML Migration](/sfml-migration) | Coming from C++ SFML |

## Requirements

- **JDK 17** or newer
- **Gradle 8+** (the Gloom repo uses Gradle 9)
- A desktop OS supported by LWJGL natives (Windows, macOS, Linux)

## Clone and run the demo

```bash
git clone <your-repo-url>
cd Gloom
./gradlew run
```

On Windows use `gradlew.bat run`.

The demo app (`org.gloom.Launcher`) exercises rendering, audio, and math splines.

## Minimal window

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

public class HelloLlw {
    public static void main(String[] args) {
        Window window = new Window(
                new WindowSettings().title("Hello LLW").size(800, 600).vsync(true));
        GraphicsContext gfx = new GraphicsContext(window);

        while (gfx.isActive()) {
            gfx.pollEvents();
            gfx.clear(new Color(25, 28, 35));
            gfx.present();
        }
        gfx.dispose();
    }
}
```

## LWJGL natives

LLW depends on LWJGL platform native JARs. When using the `:llw` Gradle module, natives are pulled in automatically as `runtimeOnly` classifiers.

If you run from the IDE, set the JVM property so LWJGL can extract natives:

```
-Dorg.lwjgl.system.SharedLibraryExtractPath=build/lwjgl-natives
```

The Gloom root `build.gradle.kts` configures this for `gradlew run`.

## Common pitfalls

- **Black screen** — make sure you call `present()` each frame; drawing alone does not swap buffers.
- **No audio** — OpenAL requires working native libraries; check console for initialization errors.
- **Wrong mouse position when zoomed** — use `Camera2d.screenToWorld()` instead of raw window pixels. See [Coordinates](/guide/coordinates).

## See also

- [Tutorial 1 — Your First Window](/tutorials/01-window)
- [Project Setup](/guide/project-setup) — depend on `:llw` from your own project
- [Minimal Window example](/examples/minimal-window)
- [Cookbook](/cookbook/mouse-picking)
- [Javadoc](/api/javadoc)
