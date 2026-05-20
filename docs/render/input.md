# Input

High-level polled input lives in `org.llw.render.input`. Low-level GLFW events and held state remain on [`Window`](window.md).

## Quick start

```java
import org.llw.render.input.Input;
import org.llw.render.window.Key;
import org.llw.render.window.MouseButton;

Input input = new Input();

while (window.isOpen()) {
    window.pollEvents();
    input.beginFrame(window);

    if (input.keyboard().isDown(Key.W)) {
        // move
    }
    if (input.mouse().wasPressed(MouseButton.LEFT)) {
        // click
    }

    // drain lifecycle events separately
    while (window.pollEvent().isPresent()) { /* Closed, Resized, ... */ }

    // draw + present
}
```

**Order matters:** `pollEvents()` → `input.beginFrame(window)` → drain `WindowEvent`s → game update.

## `Input` facade

| Accessor | Purpose |
|----------|---------|
| `keyboard()` | Held keys, press/release edges, modifiers |
| `mouse()` | Position, delta, scroll, button edges |
| `gamepads()` | Up to four GLFW joystick slots |
| `text()` | Typed characters this frame |

## Keyboard

```java
var kb = input.keyboard();

kb.isDown(Key.SPACE);          // held
kb.wasPressed(Key.ESCAPE);     // down edge this frame
kb.wasReleased(Key.ESCAPE);    // up edge this frame
kb.modifiers().shift();        // Shift held during last GLFW input callback
```

`Key` and `KeyModifiers` are defined in `org.llw.render.window`.

## Mouse

```java
var mouse = input.mouse();

mouse.position();              // window pixels, top-left origin, Y-down
mouse.delta();                 // movement since last beginFrame
mouse.scroll();                // wheel offset accumulated this frame
mouse.isDown(MouseButton.LEFT);
mouse.wasPressed(MouseButton.RIGHT);
```

Convert screen position to world space with [`Camera2d.screenToWorld`](camera.md).

`Window` also exposes `isMouseButtonDown`, `setCursorVisible`, and `setCursorLocked` for cursor control.

## Text input

```java
if (input.text().hasText()) {
    String typed = input.text().consume();
}
```

Characters come from the GLFW char callback (`WindowEvent.TextEntered` is also queued on the window).

## Gamepad

See [Gamepad](gamepad.md).

## Events vs polling

| Use polling (`Input`) | Use events (`WindowEvent`) |
|-----------------------|----------------------------|
| Movement (WASD) | Window close / resize |
| Held mouse buttons | One-shot UI shortcuts with modifiers |
| Gamepad axes | Focus gained/lost |

## See also

- [Events](events.md)
- [Window](window.md)
- [Gamepad](gamepad.md)
