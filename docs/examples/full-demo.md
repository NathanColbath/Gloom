# Full Demo

::: tip Learn step-by-step first
[Tutorial 10 — Game Loop](/tutorials/10-game-loop) builds up to this demo incrementally.
:::

The Gloom repository ships `org.gloom.Launcher` — a complete demo combining render, audio, and math.

Run it:

```bash
./gradlew run
```

## What it demonstrates

| Feature | LLW API |
|---------|---------|
| Window + game loop | `Window`, `GraphicsContext`, `Clock` |
| Sprites & shapes | `Sprite`, `Rectangle`, `Circle`, `VertexGeometry` |
| Text | `Font`, `Text` |
| Offscreen FBO | `OffscreenTarget` |
| Camera zoom/pan | `Camera2d.setSize`, `setCenter` |
| Mouse in world space | `camera.screenToWorld(mouse, getSize())` |
| Sound + music | `Sound`, `Music`, `AudioContext.update()` |
| Spline motion | `CatmullRom2f.position(t)` |

## Annotated structure

```java
// 1. Setup
Window window = new Window(settings);
GraphicsContext gfx = new GraphicsContext(window);
Camera2d camera = gfx.getCamera();

// 2. Assets
Texture2d checker = TextureFactory.checkerboard(256, 256, 32);
Sprite sprite = new Sprite(checker);
OffscreenTarget offscreen = new OffscreenTarget(gfx.backend(), new IntSize(420, 260));

// 3. Audio (optional)
AudioContext audio = new AudioContext();
Sound click = audio.createSound();
click.setBuffer(audio.loadSoundBuffer("llw/audio/samples/click.wav"));

// 4. Spline path for orb
CatmullRom2f orbPath = new CatmullRom2f(p0, p1, p2, p3);

// 5. Frame loop
while (gfx.isActive()) {
    float dt = clock.tick();
    gfx.pollEvents();
    // input → panX, panY, zoom

    camera.setCenter(size.width()/2f + panX, size.height()/2f + panY);
    camera.setSize(size.width() * zoom, size.height() * zoom);

    label.setPosition(camera.screenToWorld(window.mousePosition(), gfx.getSize()));
    orb.setPosition(orbPath.position(time * 0.15f % 1f));

    offscreen.clear(...); offscreen.draw(...); offscreen.flush();
    gfx.clear(...); gfx.draw(...); gfx.present();
    audio.update();
}
```

Source: `src/main/java/org/gloom/Launcher.java` in the Gloom repo.

## See also

- [Coordinates](/guide/coordinates)
- [Splines & Noise](/math/splines-noise)
- [Getting Started](/guide/getting-started)
