package org.gloom;

import java.nio.file.Path;

import org.llw.audio.AudioContext;
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.input.Input;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;
import org.llw.resources.ResourceManager;
import org.llw.util.log.Log;
import org.llw.util.log.LogConfig;
import org.llw.util.log.LogLevel;
import org.llw.util.log.Logger;

public class Launcher {
    private static final Logger log = Log.get("Gloom");

    private Window renderWindow;
    private GraphicsContext renderContext;
    private AudioContext audioContext;
    private ResourceManager resourceManager;
    private Clock clock;
    private GameServices gameServices;
    private Game game;
    private Input input;
    public Launcher() {
        
        renderWindow = new Window(new WindowSettings()
                .size(1600, 900)
                .title("Gloom")
                .vsync(true)
                .resizable(true));
        renderContext = new GraphicsContext(renderWindow);
        audioContext = new AudioContext();
        resourceManager = new ResourceManager(renderContext.backend(), audioContext);
        clock = new Clock();
        input = new Input();

        // Shipped assets live on the classpath (src/main/resources → player.png at classpath root).
        resourceManager.registerTexture("player.png", "player.png");
        GameServices.init(resourceManager, renderContext, audioContext, renderWindow, clock, input);

        game = new Game();


        gameLoop();
    }

    public void pullEvents() {
        while (true) {
            var optional = renderWindow.pollEvent();
            if (optional.isEmpty()) {
                break;
            }

            WindowEvent event = optional.get();
            if (event instanceof WindowEvent.Closed) {
                renderWindow.requestClose();
            }
        }
    }

    public void gameLoop() {
        clock.restart();

        while (renderContext.isActive()) {
            float dt = clock.tick();
            pullEvents();
            renderContext.pollEvents();

            renderContext.clear(Color.RED);
            input.beginFrame(renderWindow);

            game.update(clock, renderContext);


            renderContext.setFrameDelta(dt);
            renderContext.present();
        }

        renderContext.dispose();
        resourceManager.dispose();
    }

    public static void main(String[] args) {
        Log.init(LogConfig.builder()
                .logDir(Path.of("logs"))
                .minLevel(LogLevel.DEBUG) 
                .frameDiagnosticsIntervalSec(50f)
                .build());
        Log.installUncaughtExceptionHandler();
        try {
            new Launcher();
        } catch (Throwable t) {
            log.error("Startup failed", t);
            throw t;
        } finally {
            Log.shutdown();
        }
    }
}
