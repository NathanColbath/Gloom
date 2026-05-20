# Scene Stack

## Problem

Games need distinct modes — main menu, gameplay, pause overlay — each with its own update/draw logic and often a different camera (menu UI in screen space vs gameplay world with pan and zoom). Hard-coding `if (inMenu)` branches across the main loop does not scale.

## Solution

Model each mode as a `Scene` and manage them with a stack. The top scene receives input and draws last; pushing pauses the scene below without destroying it. Swap `Camera2d` settings when entering and leaving a scene.

```java
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;

interface Scene {
    void onEnter(Camera2d camera, IntSize targetSize);
    void onExit(Camera2d camera);
    void update(float dt);
    void draw(GraphicsContext graphics);
    boolean handleEvent(Object event); // WindowEvent in practice
}

final class SceneStack {
    private final Deque<Scene> stack = new ArrayDeque<>();

    void push(Scene scene, Camera2d camera, IntSize size) {
        if (!stack.isEmpty()) {
            stack.peek().onExit(camera);
        }
        stack.push(scene);
        scene.onEnter(camera, size);
    }

    void pop(Camera2d camera, IntSize size) {
        if (stack.isEmpty()) {
            return;
        }
        stack.pop().onExit(camera);
        if (!stack.isEmpty()) {
            stack.peek().onEnter(camera, size);
        }
    }

    Scene current() {
        return stack.peek();
    }

    void update(float dt) {
        if (!stack.isEmpty()) {
            stack.peek().update(dt);
        }
    }

    void draw(GraphicsContext graphics) {
        for (Scene scene : stack) {
            scene.draw(graphics);
        }
    }
}
```

**Menu scene** — 1:1 pixel camera centered on the window:

```java
final class MenuScene implements Scene {
    @Override
    public void onEnter(Camera2d camera, IntSize size) {
        camera.setCenter(size.width() / 2f, size.height() / 2f);
        camera.setSize(size.width(), size.height());
    }

    @Override
    public void onExit(Camera2d camera) { }

    @Override
    public void draw(GraphicsContext graphics) {
        graphics.draw(titleText);
        graphics.draw(playButton);
    }
    // update, handleEvent ...
}
```

**Gameplay scene** — world-sized camera with pan offset:

```java
final class GameplayScene implements Scene {
    private float panX, panY;

    @Override
    public void onEnter(Camera2d camera, IntSize size) {
        camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
        camera.setSize(size.width(), size.height());
    }

    @Override
    public void draw(GraphicsContext graphics) {
        graphics.draw(levelSprite);
        graphics.draw(playerSprite);
    }
    // update moves panX/panY and refreshes camera each frame ...
}
```

Wire the stack in your main loop:

```java
SceneStack scenes = new SceneStack();
scenes.push(new MenuScene(), graphics.getCamera(), graphics.getSize());

while (graphics.isActive()) {
    float dt = clock.tick();
    graphics.pollEvents();
    // dispatch events to scenes.current().handleEvent(...)
    scenes.update(dt);
    graphics.clear(background);
    scenes.draw(graphics);
    graphics.present();
}
```

Transition from menu to game:

```java
scenes.pop(graphics.getCamera(), graphics.getSize());   // leave menu
scenes.push(new GameplayScene(), graphics.getCamera(), graphics.getSize());
```

::: details Variations

- **Push pause overlay:** Push `PauseScene` on top of `GameplayScene`; draw only the top scene for a dimmed full-screen pause, or draw the whole stack so gameplay remains visible underneath.
- **Replace instead of stack:** `setRoot(Scene)` if you never return — simpler, no `onExit`/`onEnter` pairs.
- **Per-scene assets:** Each scene owns its `Sprite` / `Music` references and disposes them in `onExit`.
- **Sub-viewport:** Use `camera.setViewport(left, top, width, height)` for split-screen or picture-in-picture within one scene.

:::

## Pitfalls

- **Stale camera:** After window resize, call `onEnter` again or update center/size in the active scene's `update` using `graphics.getSize()`, not the initial `WindowSettings` size.
- **Event consumption:** If multiple scenes are on the stack, decide whether lower scenes receive input — usually only `stack.peek()` handles events.
- **Draw order:** Drawing the entire stack paints older scenes beneath; a menu pushed on top should clear or cover the framebuffer if you do not want gameplay visible.
- **No built-in scene type:** LLW does not ship `SceneStack`; this recipe is a small pattern you own and can extend.

## See also

- [Camera](/render/camera)
- [Coordinates & Frame Loop](/guide/coordinates)
- [Crossfade Music](/cookbook/crossfade-music)
