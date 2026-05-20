# Resource Manager Setup

End-to-end recipe: register common assets, acquire handles for drawables, and shut down cleanly.

## Problem

Manual `Texture2d` / `SoundBuffer` loading scatters paths and makes level unload error-prone. You want one registry with automatic GPU/AL cleanup when refs drop to zero.

## Solution

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Music;
import org.llw.audio.Sound;
import org.llw.resources.AssetRef;
import org.llw.resources.ResourceManager;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;
import org.llw.render.renderables.Text;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

Window window = new Window(new WindowSettings().title("Game").size(1280, 720));
GraphicsContext gfx = new GraphicsContext(window);
AudioContext audio = new AudioContext();

ResourceManager assets = new ResourceManager(gfx.backend(), audio);
assets
    .registerTexture("demo.checker", "assets/checker.png")
    .registerFont("demo.font", "llw/render/fonts/Roboto-Regular.ttf", 28)
    .registerSound("demo.click", "llw/audio/samples/click.wav")
    .registerMusic("demo.ambient", "llw/audio/samples/ambient.ogg");

AssetRef<Texture2d> checkerRef = assets.acquireTexture("demo.checker");
Sprite sprite = new Sprite(checkerRef.get());

AssetRef<Font> fontRef = assets.acquireFont("demo.font");
Text label = new Text(fontRef.get());
label.setContent("Hello");

AssetRef<org.llw.audio.SoundBuffer> clickRef = assets.acquireSound("demo.click");
Sound click = audio.createSound();
click.setBuffer(clickRef.get());

Music ambient = assets.openMusic("demo.ambient");
ambient.setLooping(true);
ambient.play();

while (gfx.isActive()) {
    gfx.pollEvents();
    audio.update();
    gfx.clear(/* ... */);
    gfx.draw(sprite);
    gfx.draw(label);
    gfx.present();
}

ambient.stop();
clickRef.release();
fontRef.release();
checkerRef.release();
assets.dispose();
audio.dispose();
gfx.dispose();
```

## Tips

- Prefer try-with-resources for short-lived refs: `try (var tex = assets.acquireTexture("id")) { ... }`
- Call `assets.loadAll()` after registration for boot-time warmup (pins all non-music assets).
- Stop music before `assets.dispose()`.

## See also

- [Resources Overview](/resources/overview)
- [Asset Lifecycle](/resources/asset-lifecycle)
- [Sound Pool](/cookbook/sound-pool)
