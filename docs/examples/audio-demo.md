# Audio Demo

Play a click sound effect and looping ambient music.

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Music;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

Window window = new Window(new WindowSettings().size(800, 600));
GraphicsContext gfx = new GraphicsContext(window);
AudioContext audio = new AudioContext();

SoundBuffer clickBuf = audio.loadSoundBuffer("llw/audio/samples/click.wav");
Sound click = audio.createSound();
click.setBuffer(clickBuf);

Music ambient = audio.openMusic("llw/audio/samples/ambient.ogg");
ambient.setLooping(true);
ambient.setVolume(35f);
ambient.play();

while (gfx.isActive()) {
    gfx.pollEvents();

    while (true) {
        var event = window.pollEvent();
        if (event.isEmpty()) break;
        if (event.get() instanceof WindowEvent.KeyPressed k && k.key() == Key.SPACE) {
            click.play();
        }
    }

    audio.update();
    gfx.clear(new Color(20, 22, 30));
    gfx.present();
}

audio.dispose();
gfx.dispose();
```

## Common pitfalls

- Wrap `new AudioContext()` in try/catch if audio is optional on the target machine.
- Always call `audio.update()` in the game loop while music plays.

## See also

- [Audio Overview](/audio/overview)
- [Sounds](/audio/sounds)
- [Music](/audio/music)
