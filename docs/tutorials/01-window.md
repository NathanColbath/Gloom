# Tutorial 1 — Your First Window

## Goal

Open an LLW window, attach a `GraphicsContext`, and run a minimal game loop that clears the framebuffer each frame and presents it to the screen. By the end you will understand how `WindowSettings`, `Window`, and the clear/present cycle fit together.

## Prerequisites

- A Java 21+ project with the LLW module on the classpath (see [Getting Started](/guide/getting-started) and [Project Setup](/guide/project-setup)).
- No prior tutorials required — this is the starting point.

## Step 1 — Configure the window

`WindowSettings` holds everything GLFW needs before the window exists: title, initial size, whether the user can resize, and vsync preference. Setters return `this` so you can chain them fluently.

```java
import org.llw.render.window.WindowSettings;

WindowSettings settings = new WindowSettings()
        .title("Tutorial 01 — Window")
        .size(960, 540)
        .resizable(true)
        .vsync(true);
```

::: tip Defaults
If you omit configuration, LLW uses title `"Gloom"`, size `1280×720`, resizable `true`, and vsync `true`.
:::

## Step 2 — Create the window

Pass your settings to `Window`. The constructor initializes GLFW, creates an OpenGL 3.3 core profile context, centers the window on the primary monitor, and shows it.

```java
import org.llw.render.window.Window;

Window window = new Window(settings);
```

::: warning Main thread
`Window` must be created, polled, and destroyed on the same thread — typically `main`. GLFW callbacks and input state only update when you call `pollEvents()` on that thread.
:::

## Step 3 — Attach a graphics context

`GraphicsContext` wraps the window's default framebuffer and an OpenGL backend. It configures a 2D camera to match the initial window size with Y-down coordinates (origin at the top-left, Y increasing downward).

```java
import org.llw.render.graphics.GraphicsContext;

GraphicsContext graphics = new GraphicsContext(window);
```

## Step 4 — Run the loop

Each frame follows a short lifecycle:

1. `pollEvents()` — GLFW processes OS messages and input callbacks.
2. `clear(Color)` — set the GL clear color and wipe the back buffer.
3. `present()` — flush queued draws and swap front/back buffers.

Use `graphics.isActive()` as your loop condition. It stays `true` while the window is open and the context has not been disposed.

```java
import org.llw.render.core.Color;

while (graphics.isActive()) {
    graphics.pollEvents();
    graphics.clear(new Color(28, 32, 42));
    graphics.present();
}
```

Try changing the `Color` arguments — each channel is an 8-bit value from `0` to `255`. The constructor used here is opaque RGB; pass a fourth argument for alpha.

::: details Why `present()` instead of `swapBuffers()`?
`GraphicsContext.present()` calls `flush()` (submits any batched draw commands) and then `window.swapBuffers()`. For a clear-only loop the flush is a no-op, but using `present()` keeps your frame boundary consistent once you start calling `draw()`.
:::

## Step 5 — Shut down cleanly

When the loop exits, release GPU resources and terminate GLFW in one call:

```java
graphics.dispose();
```

`dispose()` marks the context inactive, tears down the OpenGL backend, and destroys the window. Do not call `present()` or `draw()` after disposal.

## Complete example

Save this as `Tutorial01Window.java` and run it from your project:

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

public class Tutorial01Window {
    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings()
                .title("Tutorial 01 — Window")
                .size(960, 540)
                .resizable(true)
                .vsync(true);

        Window window = new Window(settings);
        GraphicsContext graphics = new GraphicsContext(window);

        while (graphics.isActive()) {
            graphics.pollEvents();
            graphics.clear(new Color(28, 32, 42));
            graphics.present();
        }

        graphics.dispose();
    }
}
```

You should see a dark blue-gray window. Close it with the title-bar button to exit (handling that close request properly is the subject of the next tutorial).

## What you learned

- `WindowSettings` configures title, size, resizability, and vsync before window creation.
- `Window` owns the GLFW handle and must live on the main thread.
- `GraphicsContext` connects the window to OpenGL and exposes `clear`, `draw`, and `present`.
- The per-frame pattern is **poll → clear → draw (later) → present**.
- `graphics.dispose()` releases GPU resources and destroys the window.

## Next

Continue to [Tutorial 2 — Events & Input](/tutorials/02-events) to process `WindowEvent` instances and close the window with the Escape key.
