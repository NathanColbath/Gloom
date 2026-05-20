# Tutorial 2 — Events & Input

## Goal

Extend your window loop to drain the event queue with `pollEvent`, react to `WindowEvent` variants (close, resize, keys, mouse), and query held keys with `isKeyDown`. You will close the application when the user presses Escape or clicks the title-bar close button.

## Prerequisites

Complete [Tutorial 1 — Your First Window](/tutorials/01-window). You should already have a running clear/present loop with `WindowSettings`, `Window`, and `GraphicsContext`.

## Step 1 — Start from the window loop

Keep the setup from tutorial 1. The only change in this tutorial is what happens inside the loop before you clear the screen.

```java
WindowSettings settings = new WindowSettings()
        .title("Tutorial 02 — Events")
        .size(960, 540);

Window window = new Window(settings);
GraphicsContext graphics = new GraphicsContext(window);
```

## Step 2 — Poll GLFW, then drain the queue

`graphics.pollEvents()` forwards to `window.pollEvents()`, which runs GLFW callbacks. Those callbacks enqueue `WindowEvent` records internally. After polling, pull events one at a time with `window.pollEvent()` until the queue is empty.

```java
graphics.pollEvents();

while (true) {
  var optionalEvent = window.pollEvent();
  if (optionalEvent.isEmpty()) {
    break;
  }
  WindowEvent event = optionalEvent.get();
  // handle event below
}
```

::: tip Poll once per frame
Call `pollEvents()` exactly once at the start of each frame, then drain the queue. Draining in an inner `while` loop ensures you never miss bursts of input (for example, several key repeats in one frame).
:::

## Step 3 — Handle `WindowEvent` variants

`WindowEvent` is a sealed interface. Use `instanceof` with pattern matching to branch on the record type:

| Event | When it fires |
|-------|----------------|
| `Closed` | User clicked the title-bar close button |
| `Resized` | Framebuffer width/height changed |
| `KeyPressed` / `KeyReleased` | Keyboard action |
| `MouseMoved` | Cursor moved in the client area |
| `MouseButtonPressed` / `MouseButtonReleased` | Mouse button action |
| `MouseScrolled` | Scroll wheel moved |

```java
import org.llw.render.window.WindowEvent;

if (event instanceof WindowEvent.Closed) {
    graphics.window().requestClose();
} else if (event instanceof WindowEvent.Resized resized) {
    // Tutorial 8 covers camera updates in depth; for now, note the new size:
    int w = resized.width();
    int h = resized.height();
} else if (event instanceof WindowEvent.KeyPressed keyPressed) {
    // React to individual key presses (fires once per physical press)
} else if (event instanceof WindowEvent.MouseMoved moved) {
    float x = moved.position().x;
    float y = moved.position().y;
}
```

When the user clicks the close button, GLFW emits `Closed`. Call `requestClose()` so `graphics.isActive()` becomes `false` on the next iteration.

## Step 4 — Close on Escape

Add an explicit quit path with the `Key` enum. Compare the key from `KeyPressed` events:

```java
import org.llw.render.window.Key;

} else if (event instanceof WindowEvent.KeyPressed keyPressed
        && keyPressed.key() == Key.ESCAPE) {
    graphics.window().requestClose();
}
```

::: warning Events vs. held state
`KeyPressed` fires once when a key goes down. For continuous movement (holding W to walk), use `isKeyDown` in the next step instead of counting repeated `KeyPressed` events.
:::

## Step 5 — Query held keys with `isKeyDown`

After processing the event queue, you can read the current keyboard state. `Window` tracks which keys are down and updates the set during `pollEvents()`.

```java
if (window.isKeyDown(Key.SPACE)) {
    // Runs every frame while Space is held
}
```

For this tutorial we only use `isKeyDown` with Escape as a belt-and-suspenders check — some platforms deliver close via events only, but polling held Escape is useful when you add pause menus later.

## Step 6 — Animate the clear color from input

Tie a simple visual to input so you can see events working. Shift the background toward blue while **R** is held:

```java
import org.llw.render.core.Color;

int r = 28;
int g = 32;
int b = 42;
if (window.isKeyDown(Key.R)) {
    b = 120;
}
graphics.clear(new Color(r, g, b));
graphics.present();
```

Press **R** to tint the window; press **Escape** or click close to exit.

::: details Event type reference
`MouseButtonPressed` and `MouseButtonReleased` carry a `MouseButton` (`LEFT`, `RIGHT`, `MIDDLE`) and the cursor position at the time of the action. `MouseScrolled` provides `xOffset()` and `yOffset()` where positive vertical offset means scroll up.
:::

## Complete example

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class Tutorial02Events {
    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings()
                .title("Tutorial 02 — Events")
                .size(960, 540);

        Window window = new Window(settings);
        GraphicsContext graphics = new GraphicsContext(window);

        while (graphics.isActive()) {
            graphics.pollEvents();

            while (true) {
                var optionalEvent = window.pollEvent();
                if (optionalEvent.isEmpty()) {
                    break;
                }
                WindowEvent event = optionalEvent.get();
                if (event instanceof WindowEvent.Closed) {
                    graphics.window().requestClose();
                } else if (event instanceof WindowEvent.KeyPressed keyPressed
                        && keyPressed.key() == Key.ESCAPE) {
                    graphics.window().requestClose();
                }
            }

            int r = 28;
            int g = 32;
            int b = 42;
            if (window.isKeyDown(Key.R)) {
                b = 120;
            }

            graphics.clear(new Color(r, g, b));
            graphics.present();
        }

        graphics.dispose();
    }
}
```

## What you learned

- `pollEvents()` must run once per frame before reading events or key state.
- `window.pollEvent()` returns `Optional<WindowEvent>`; drain until empty.
- `WindowEvent` is a sealed hierarchy — use `instanceof` to handle `Closed`, `Resized`, keys, and mouse variants.
- `requestClose()` ends the main loop cleanly; handle `Closed` from the title bar.
- `isKeyDown(Key)` reflects keys currently held after the latest poll.

## Next

Continue to [Tutorial 3 — Textures](/tutorials/03-textures) to allocate GPU images with `Texture2d` and procedural helpers from `TextureFactory`.
