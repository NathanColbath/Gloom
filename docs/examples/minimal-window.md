# Minimal Window

::: tip Prefer a guided path?
This example is also [Tutorial 1](/tutorials/01-window) in the 10-chapter track.
:::

The smallest LLW program: open a window, clear each frame, exit on close.

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class MinimalWindow {
    public static void main(String[] args) {
        Window window = new Window(
                new WindowSettings().title("Minimal LLW").size(800, 600).vsync(true));
        GraphicsContext gfx = new GraphicsContext(window);

        while (gfx.isActive()) {
            gfx.pollEvents();

            while (true) {
                var event = window.pollEvent();
                if (event.isEmpty()) break;
                if (event.get() instanceof WindowEvent.Closed) {
                    gfx.window().requestClose();
                }
            }

            gfx.clear(new Color(25, 28, 35));
            gfx.present();
        }
        gfx.dispose();
    }
}
```

## See also

- [Getting Started](/guide/getting-started)
- [Window](/render/window)
