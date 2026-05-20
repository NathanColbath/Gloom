package org.gloom;

import org.llw.math.transform.Transform2f;
import org.llw.math.vector.Vector2f;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.Key;

public class Player {

    private Sprite sprite;
    private Transform2f transform;
    private Texture2d texture;

    public Player(){
        sprite = new Sprite(GameServices.getResourceManager().acquireTexture("player.png").get());
        sprite.setOrigin(sprite.getTexture().size().width() / 2f, sprite.getTexture().size().height() / 2f);
        transform = new Transform2f();
        transform.setPosition(0, 0);
        transform.setRotation(0);
        transform.setScale(1, 1);
        transform.setOrigin(0, 0);
    }

    public void update(){
        sprite.setPosition(transform.getPosition());
        sprite.setRotation(transform.getRotation());

        if(GameServices.getInput().keyboard().isDown(Key.A))
        {
            transform.setPosition(transform.getPosition().x - 1, transform.getPosition().y);
        }
        if(GameServices.getInput().keyboard().isDown(Key.D))
        {
            transform.setPosition(transform.getPosition().x + 1, transform.getPosition().y);
        }

        if(GameServices.getInput().keyboard().isDown(Key.W))
        {
            transform.setPosition(transform.getPosition().x, transform.getPosition().y - 1);
        }
        if(GameServices.getInput().keyboard().isDown(Key.S))
        {
            transform.setPosition(transform.getPosition().x, transform.getPosition().y + 1);
        }

        Vector2f mouseScreen = GameServices.getInput().mouse().position();
        Camera2d camera = GameServices.getRenderContext().getCamera();

        Vector2f target = camera.screenToWorld(mouseScreen, GameServices.getRenderContext().getSize());

        Vector2f pos = transform.getPosition();

        float dx = target.x - pos.x;
        float dy = target.y - pos.y;

        float angle = (float) Math.atan2(dy, dx);

        transform.setRotation(angle);
    }

    public Sprite getSprite(){
        return sprite;
    }

    public Transform2f getTransform(){
        return transform;
    }

}
