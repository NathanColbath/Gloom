package org.gloom;

import org.llw.audio.AudioContext;
import org.llw.render.core.Clock;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.input.Input;
import org.llw.render.window.Window;
import org.llw.resources.ResourceManager;

public class GameServices {

    private  static ResourceManager resourceManager;
    private  static GraphicsContext graphicsContext;
    private  static AudioContext audioContext;
    private  static Input input;
    private  static Window window;
    private  static Clock clock;

    public static void init(ResourceManager resourceManager, GraphicsContext renderContext, AudioContext audioContext, Window window, Clock clock, Input input) {
        GameServices.resourceManager = resourceManager;
        GameServices.graphicsContext = renderContext;
        GameServices.audioContext = audioContext;
        GameServices.input = new Input();
        GameServices.window = window;
        GameServices.clock = clock;
        GameServices.input = input;
    }
    
    public static ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    public static GraphicsContext getRenderContext() {
        return graphicsContext;
    }
    
    public static AudioContext getAudioContext() {
        return audioContext;
    }
    
    public static Window getWindow() {
        return window;
    }
    
    public static Clock getClock() {
        return clock;
    }
    
    public static Input getInput() {
        return input;
    }
}
