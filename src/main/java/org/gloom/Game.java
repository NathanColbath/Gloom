package org.gloom;

import org.llw.render.core.Clock;
import org.llw.render.graphics.GraphicsContext;

public class Game {


    private Player player;

    public Game(){
        player = new Player();
    }

    public void update(Clock clock, GraphicsContext graphicsContext){
        player.update();

        //player.getTransform().setPosition(GameServices.getWindow().mousePosition().x, GameServices.getWindow().mousePosition().y);

        graphicsContext.draw(player.getSprite());
    }

}
