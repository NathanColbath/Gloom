package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.physics.PhysicsTransformSync;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: local and world transform for an entity.
 */
public final class TransformBinding {
    /** Local position in parent space. */
    @HostAccess.Export
    public final Vector2Binding position;
    /** Local scale. */
    @HostAccess.Export
    public final Vector2Binding scale;
    private final World world;
    private final EntityId entity;

    /**
     * @param world  play-mode ECS world
     * @param entity target entity
     */
    public TransformBinding(World world, EntityId entity) {
        this.world = world;
        this.entity = entity;
        this.position = new Vector2Binding(this::readPosition, this::writePosition);
        this.scale = new Vector2Binding(this::readScale, this::writeScale);
    }

    /**
     * @return local rotation in degrees
     */
    @HostAccess.Export
    public double getRotation() {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        return transform == null ? 0d : transform.rotation;
    }

    /**
     * @param value local rotation in degrees
     */
    @HostAccess.Export
    public void setRotation(double value) {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform != null) {
            transform.rotation = (float) value;
            PhysicsTransformSync.markDirty(world, entity);
        }
    }

    /**
     * @return world-space X
     */
    @HostAccess.Export
    public double getWorldX() {
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        return worldTransform == null ? readPosition()[0] : worldTransform.worldX;
    }

    /**
     * @param value desired world X (converted to local when parented)
     */
    @HostAccess.Export
    public void setWorldX(double value) {
        float localX = (float) value;
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        if (worldTransform != null) {
            float[] local = readPosition();
            float dx = (float) value - worldTransform.worldX;
            localX = local[0] + dx;
        }
        writePosition(localX, readPosition()[1]);
    }

    /**
     * @return world-space Y
     */
    @HostAccess.Export
    public double getWorldY() {
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        return worldTransform == null ? readPosition()[1] : worldTransform.worldY;
    }

    /**
     * @param value desired world Y (converted to local when parented)
     */
    @HostAccess.Export
    public void setWorldY(double value) {
        float localY = (float) value;
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        if (worldTransform != null) {
            float[] local = readPosition();
            float dy = (float) value - worldTransform.worldY;
            localY = local[1] + dy;
        }
        writePosition(readPosition()[0], localY);
    }

    /**
     * @param dx local X delta
     * @param dy local Y delta
     */
    @HostAccess.Export
    public void translate(double dx, double dy) {
        float[] position = readPosition();
        writePosition(position[0] + (float) dx, position[1] + (float) dy);
    }

    private float[] readPosition() {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform == null) {
            return new float[]{0f, 0f};
        }
        return new float[]{transform.x, transform.y};
    }

    private void writePosition(float x, float y) {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform != null) {
            transform.x = x;
            transform.y = y;
            PhysicsTransformSync.markDirty(world, entity);
        }
    }

    private float[] readScale() {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform == null) {
            return new float[]{1f, 1f};
        }
        return new float[]{transform.scaleX, transform.scaleY};
    }

    private void writeScale(float x, float y) {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform != null) {
            transform.scaleX = x;
            transform.scaleY = y;
            PhysicsTransformSync.markDirty(world, entity);
        }
    }

    /** Mutable 2D vector backed by transform component fields. */
    public static final class Vector2Binding {
        private final java.util.function.Supplier<float[]> reader;
        private final java.util.function.BiConsumer<Float, Float> writer;

        Vector2Binding(
                java.util.function.Supplier<float[]> reader,
                java.util.function.BiConsumer<Float, Float> writer
        ) {
            this.reader = reader;
            this.writer = writer;
        }

        /** @return X component */
        @HostAccess.Export
        public double getX() {
            return reader.get()[0];
        }

        /**
         * @param value new X component
         */
        @HostAccess.Export
        public void setX(double value) {
            float[] current = reader.get();
            writer.accept((float) value, current[1]);
        }

        /** @return Y component */
        @HostAccess.Export
        public double getY() {
            return reader.get()[1];
        }

        /**
         * @param value new Y component
         */
        @HostAccess.Export
        public void setY(double value) {
            float[] current = reader.get();
            writer.accept(current[0], (float) value);
        }
    }
}
