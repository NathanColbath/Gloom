# WindowSettings

`org.llw.render.window.WindowSettings` is a mutable, fluent builder passed to `Window` at construction time. It captures title, initial size, resizability, and vsync preference. Setters return `this` for chaining.

## Defaults

| Field | Default |
|-------|---------|
| Title | `"Gloom"` |
| Width × height | `1280 × 720` |
| Resizable | `true` |
| Vsync | `true` |

::: tip
`WindowSettings` is a snapshot at construction — `Window.settings()` returns the values used when the window was created, not live runtime state. Use `Window.size()` for the current framebuffer dimensions.
:::

## Key methods

| Method | Returns | Description |
|--------|---------|-------------|
| `title()` / `title(String)` | `String` / `WindowSettings` | Window title bar text |
| `width()` / `height()` | `int` | Initial dimensions in screen coordinates |
| `size(int width, int height)` | `WindowSettings` | Sets both width and height |
| `resizable()` / `resizable(boolean)` | `boolean` / `WindowSettings` | Whether the user can resize the window |
| `vsync()` / `vsync(boolean)` | `boolean` / `WindowSettings` | Vertical sync for buffer swaps |

## Examples

```java
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

WindowSettings settings = new WindowSettings()
        .title("My Game")
        .size(1920, 1080)
        .resizable(false)
        .vsync(true);

Window window = new Window(settings);
```

Borderless-style fixed layout:

```java
WindowSettings settings = new WindowSettings()
        .title("Debug HUD")
        .size(800, 600)
        .resizable(false)
        .vsync(false);  // uncapped frame rate for profiling
```

## Pitfalls

::: warning
On high-DPI displays, the logical size from `WindowSettings` may differ from the drawable framebuffer size returned by `Window.size()`. Always size cameras and projection from `Window.size()` or `GraphicsContext.getSize()`, not from cached settings.
:::

- Vsync is stored in settings but applied through GLFW window hints at creation; changing `vsync()` after the window exists has no effect.
- There is no fullscreen or monitor-index API on `WindowSettings` yet — only centered placement on the primary monitor.

## See also

- [Window](/render/window)
- [Events](/render/events)
- [Graphics Context](/render/graphics-context)
