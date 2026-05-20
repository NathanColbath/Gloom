# WindowEvent

`org.llw.render.window.WindowEvent` is a **sealed interface** describing lifecycle, input, and resize notifications from `Window`. GLFW callbacks enqueue events during `Window.pollEvents()` (or `GraphicsContext.pollEvents()`); you dequeue them with `Window.pollEvent()`.

## Event types

| Record | Components | When emitted |
|--------|------------|--------------|
| `Closed` | — | User or system requests close (title-bar ×, etc.) |
| `Resized` | `width`, `height` | Framebuffer size changed (pixels) |
| `FocusGained` | — | Window gained keyboard focus |
| `FocusLost` | — | Window lost keyboard focus |
| `KeyPressed` | `key`, `mods`, `repeated` | Key transitioned to pressed (or repeat) |
| `KeyReleased` | `key`, `mods` | Key transitioned to released |
| `TextEntered` | `codepoint` | Unicode character typed |
| `MouseMoved` | `position` | Cursor moved in client area |
| `MouseButtonPressed` | `button`, `position`, `mods` | Mouse button pressed |
| `MouseButtonReleased` | `button`, `position`, `mods` | Mouse button released |
| `MouseScrolled` | `xOffset`, `yOffset` | Scroll wheel rotated |

All mouse positions use **window coordinates**: origin at the top-left of the client area, Y increasing downward.

## Processing events

```java
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Key;
import org.llw.render.window.WindowEvent;

void handleFrameEvents(GraphicsContext gfx) {
    gfx.pollEvents();

    while (true) {
        var opt = gfx.window().pollEvent();
        if (opt.isEmpty()) break;

        switch (opt.get()) {
            case WindowEvent.Closed closed ->
                    gfx.window().requestClose();

            case WindowEvent.Resized r -> {
                var cam = gfx.getCamera();
                cam.setCenter(r.width() / 2f, r.height() / 2f);
                cam.setSize(r.width(), r.height());
            }

            case WindowEvent.KeyPressed k when k.key() == Key.ESCAPE ->
                    gfx.window().requestClose();

            case WindowEvent.MouseButtonPressed m when m.button() == MouseButton.LEFT -> {
                Vector2f world = gfx.getCamera()
                        .screenToWorld(m.position(), gfx.getSize());
                onClick(world);
            }

            case WindowEvent.MouseScrolled s ->
                    zoom += s.yOffset() * 0.1f;

            default -> {}
        }
    }
}
```

Resize-only drain (skip input handling):

```java
while (true) {
    var event = window.pollEvent();
    if (event.isEmpty()) break;
    if (event.get() instanceof WindowEvent.Resized r) {
        onResize(r.width(), r.height());
    }
}
```

## Pitfalls

::: warning
Events are **not** delivered until `pollEvents()` runs on the main thread. Querying `isKeyDown()` or `mousePosition()` before polling reflects the previous frame's state.
:::

- `KeyPressed` includes `mods` and `repeated` (GLFW key repeat). `KeyReleased` includes `mods`.
- `TextEntered` delivers a Unicode codepoint from the char callback (see also `Input.text()`).
- `MouseMoved` includes position copies; mutating the returned `Vector2f` does not affect internal state.
- `Closed` also sets the internal close flag; you can call `requestClose()` programmatically without waiting for the event.

::: tip
For continuous movement (WASD), prefer `Input.keyboard().isDown(Key)` after `input.beginFrame(window)` instead of tracking press/release events. See [Input](input.md).
:::

## See also

- [Window](/render/window)
- [Input](/render/input)
- [Camera](/render/camera) — convert `MouseMoved` / button positions to world space
