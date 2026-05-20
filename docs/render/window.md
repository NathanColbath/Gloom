# Window

`org.llw.render.window` wraps GLFW for window creation, input, and events. `Window` owns an OpenGL 3.3 core profile context and must be polled on the **main thread** that constructed it.

## In this section

| Topic | Page |
|-------|------|
| GLFW window API | [Window](/render/window) (this page) |
| Construction settings | [WindowSettings](/render/window-settings) |
| `WindowEvent` records | [Events](/render/events) |
| `Key`, `MouseButton`, polling | [Input](/render/input) |

## Key types

| Type | Purpose |
|------|---------|
| `WindowSettings` | Builder for title, size, vsync, resizable |
| `Window` | Native handle, event queue, input state, buffer swap |
| `WindowEvent` | Sealed events: `Closed`, `Resized`, `KeyPressed`, `MouseMoved`, … |
| `Key`, `MouseButton` | Input enums mapped from GLFW |

## Creating a window

```java
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

WindowSettings settings = new WindowSettings()
        .title("My Game")
        .size(1280, 720)
        .vsync(true)
        .resizable(true);

Window window = new Window(settings);
```

## Event loop

```java
import org.llw.render.window.Key;
import org.llw.render.window.WindowEvent;

while (true) {
    var event = window.pollEvent();
    if (event.isEmpty()) break;

    switch (event.get()) {
        case WindowEvent.Closed closed -> gfx.window().requestClose();
        case WindowEvent.Resized r -> {
            camera.setCenter(r.width() / 2f, r.height() / 2f);
            camera.setSize(r.width(), r.height());
        }
        case WindowEvent.KeyPressed k when k.key() == Key.ESCAPE ->
                gfx.window().requestClose();
        default -> {}
    }
}
```

Prefer `GraphicsContext.pollEvents()` in game loops — it forwards to the same GLFW poll.

## Input polling

```java
if (window.isKeyDown(Key.W)) player.moveUp(dt);
Vector2f mouse = window.mousePosition(); // pixels, top-left origin
```

Convert mouse to world space via the camera — see [Camera](/render/camera).

## Window API highlights

| Method | Description |
|--------|-------------|
| `handle()` | Native GLFW pointer |
| `settings()` | Snapshot used at construction |
| `size()` | Current **framebuffer** size (`IntSize`) |
| `isOpen()` | Main-loop guard |
| `requestClose()` | Close on next boundary |
| `pollEvents()` / `swapBuffers()` | Per-frame GLFW |
| `pollEvent()` | Dequeue one `WindowEvent` |
| `isKeyDown(Key)` | Held key after last poll |
| `mousePosition()` | Cursor copy in client pixels |
| `destroy()` | Destroy window and terminate GLFW |

## Common pitfalls

- Events are only delivered after `pollEvents()` — see [Events](/render/events).
- `mousePosition()` returns **screen pixels**, not world coordinates.
- `destroy()` terminates GLFW globally; only one `Window` lifecycle per process is supported today.

## See also

- [WindowSettings](/render/window-settings)
- [Graphics Context](/render/graphics-context)
- [Coordinates](/guide/coordinates)
