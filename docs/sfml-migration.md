# SFML Migration

LLW deliberately mirrors SFML's 2D API shape on top of LWJGL. This page maps common SFML types and calls to `org.llw.*` equivalents.

## Philosophy

| SFML (C++) | LLW (Java) |
|------------|------------|
| `sf::RenderWindow` | `Window` + `GraphicsContext` |
| Immediate-mode draw on window | Queued draw + `present()` |
| `sf::Vector2f` | `org.llw.math.vector.Vector2f` |
| `sf::Vector3f` (audio) | `org.llw.audio.core.Vector3f` |
| Header + `.cpp` linking | Gradle module `:llw` + LWJGL natives |

Coordinates are **Y-down** (top-left origin), matching SFML's default view.

---

## Window & events

| SFML | LLW | Notes |
|------|-----|-------|
| `sf::VideoMode`, `sf::Style` | `WindowSettings` | Fluent builder: `.title()`, `.size()`, `.vsync()`, `.resizable()` |
| `sf::RenderWindow` | `Window` + `GraphicsContext` | GL context owned by `Window`; drawing through `GraphicsContext` |
| `window.pollEvent(event)` | `window.pollEvent()` → `Optional<WindowEvent>` | Java queue instead of reference out-param |
| `window.isOpen()` | `gfx.isActive()` | Also false after `dispose()` |
| `window.display()` | `gfx.present()` | Flushes draw queue + swap buffers |
| `window.clear(color)` | `gfx.clear(color)` | |
| `window.setFramerateLimit` | VSync via `WindowSettings.vsync(true)` | No soft FPS cap API yet |
| `sf::Keyboard::Key` | `org.llw.render.window.Key` | |
| `sf::Mouse::Button` | `MouseButton` | |
| `event.mouseButton` / `key` | `WindowEvent` types | See [Events](/render/events) |

---

## Drawing & resources

| SFML | LLW | Notes |
|------|-----|-------|
| `sf::Texture` | `Texture2d` | `Texture2d.fromClasspath`, `fromFile`, `TextureFactory` helpers |
| `sf::Sprite` | `org.llw.render.renderables.Sprite` | `setTexture`, `setTextureRect` via sub-rect on texture |
| `sf::RectangleShape` | `Rectangle` | Fill + outline |
| `sf::CircleShape` | `org.llw.render.renderables.Circle` | |
| `sf::Text` | `Text` + `Font` | |
| `sf::VertexArray` | `VertexGeometry` | Custom primitives |
| `sf::Color` | `org.llw.render.core.Color` | RGBA 0–255 |
| `sf::View` | `Camera2d` | Via `gfx.getCamera()` |
| `sf::RenderTexture` | `OffscreenTarget` | `flush()` instead of `display()` |
| `sf::Shader` | `ShaderProgram` | GLSL 330 core |
| `sf::Transform` / `Transformable` | `Transform2f` / `Transformable` | Same pivot model |
| `sf::Clock` | `org.llw.render.core.Clock` | `tick()` → delta seconds |

---

## Audio

| SFML | LLW | Notes |
|------|-----|-------|
| `sf::SoundBuffer` | `SoundBuffer` | Load via `AudioContext.loadSoundBuffer` |
| `sf::Sound` | `Sound` | `audio.createSound()` |
| `sf::Music` | `Music` | `audio.openMusic`; requires `audio.update()` |
| `sf::SoundSource` | `Sound` / `Music` shared API | Volume 0–100, pitch, pan, 3D position |
| `sf::Listener` | `AudioListener` | **Static** methods, not instance |
| `sf::Time` | `org.llw.audio.core.Time` | `asSeconds()` |
| `sf::SoundSource::Status` | `PlaybackStatus` | `PLAYING`, `PAUSED`, `STOPPED` |

::: warning No sf::SoundBufferRecorder
LLW does not expose recording. Capture externally and load WAV.
:::

---

## Math

| SFML | LLW | Notes |
|------|-----|-------|
| `sf::Vector2f` | `Vector2f` | In `org.llw.math.vector` |
| `sf::Vector3f` (graphics) | N/A | 2D-focused; audio has separate `Vector3f` |
| `sf::Rect<T>` | `RectF` | `left, top, width, height` |
| `sf::Transform` | `Matrix3x2` + `Transform2f` | 4×4 column-major for GL |
| Manual collision | `Intersection2`, `Sat2f`, geometry types | See [Collision](/math/collision) |

SFML's `sf::FloatRect` intersection helpers map to `RectF.intersects`, `union`, `intersection`.

---

## Code shape comparison

### SFML (C++)

```cpp
sf::RenderWindow window(sf::VideoMode(800, 600), "Game");
sf::Texture texture;
texture.loadFromFile("sprite.png");
sf::Sprite sprite(texture);

while (window.isOpen()) {
    sf::Event event;
    while (window.pollEvent(event)) { /* ... */ }
    window.clear(sf::Color::Black);
    window.draw(sprite);
    window.display();
}
```

### LLW (Java)

```java
Window window = new Window(new WindowSettings().title("Game").size(800, 600));
GraphicsContext gfx = new GraphicsContext(window);
Sprite sprite = new Sprite(Texture2d.fromClasspath("assets/sprite.png"));

while (gfx.isActive()) {
    gfx.pollEvents();
    while (window.pollEvent(e).isPresent()) { /* ... */ }
    gfx.clear(Color.BLACK);
    gfx.draw(sprite);
    gfx.present();
}
gfx.dispose();
```

---

## Not ported / different

| SFML feature | LLW status |
|--------------|------------|
| Network module | Not in LLW |
| `sf::RenderWindow` built-in `draw` overloads for shapes | Use `gfx.draw(renderable)` |
| `sf::Thread` | Use Java `ExecutorService` / virtual threads |
| `sf::Utf` | Java `String` (UTF-16) |
| Joystick / gamepad | GLFW gamepad API not wrapped yet — use LWJGL GLFW directly |
| `setActive(false)` context switching | Single context per window |
| MP3 audio | WAV + OGG only |

---

## Gloom rename (pre-LLW Java packages)

If you migrated from the original Gloom package names:

| Legacy | Current |
|--------|---------|
| `org.gloom.renderbackend.*` | `org.llw.render.*` |
| `org.gloom.audiobackend.*` | `org.llw.audio.*` |
| `org.gloom.math.*` | `org.llw.math.*` |

Subpackage structure is preserved (`graphics`, `window`, `vector`, etc.).

## See also

- [Getting Started](/guide/getting-started)
- [Audio Overview](/audio/overview)
- [Render Overview](/render/overview)
- [FAQ](/faq)
