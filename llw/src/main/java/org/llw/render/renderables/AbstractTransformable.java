package org.llw.render.renderables;

import org.llw.math.matrix.Matrix3x2;
import org.llw.math.transform.Transform2f;
import org.llw.math.vector.Vector2f;
import org.llw.render.graphics.Transformable;

/**
 * Base implementation of {@link Transformable} delegating to {@link Transform2f}.
 */
public abstract class AbstractTransformable implements Transformable {
    private final Transform2f transform = new Transform2f();

    @Override
    public Vector2f getPosition() {
        return transform.getPosition();
    }

    @Override
    public void setPosition(float x, float y) {
        transform.setPosition(x, y);
    }

    @Override
    public void setPosition(Vector2f position) {
        setPosition(position.x, position.y);
    }

    @Override
    public float getRotation() {
        return transform.getRotation();
    }

    @Override
    public void setRotation(float radians) {
        transform.setRotation(radians);
    }

    @Override
    public Vector2f getScale() {
        return transform.getScale();
    }

    @Override
    public void setScale(float x, float y) {
        transform.setScale(x, y);
    }

    @Override
    public void setScale(Vector2f scale) {
        setScale(scale.x, scale.y);
    }

    @Override
    public Vector2f getOrigin() {
        return transform.getOrigin();
    }

    @Override
    public void setOrigin(float x, float y) {
        transform.setOrigin(x, y);
    }

    @Override
    public void setOrigin(Vector2f origin) {
        setOrigin(origin.x, origin.y);
    }

    @Override
    public Matrix3x2 getTransform() {
        return transform.toMatrix();
    }

    /** Marks the cached matrix stale on the next {@link #getTransform()} call. */
    protected void markDirty() {
        transform.markDirty();
    }
}
