package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.SpriteRendererComponent;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: {@code SpriteRenderer} component host binding.
 */
public final class SpriteRendererBinding {
    private final World world;
    private final EntityId entity;

    /**
     * @param context play-mode script context
     * @param entity  owning entity
     */
    public SpriteRendererBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    /**
     * @return sprite slice asset GUID
     */
    @HostAccess.Export
    public String getSpriteGuid() {
        SpriteRendererComponent sprite = component();
        return sprite == null ? "" : sprite.spriteGuid;
    }

    /**
     * @param guid sprite slice asset GUID
     */
    @HostAccess.Export
    public void setSpriteGuid(String guid) {
        SpriteRendererComponent sprite = component();
        if (sprite != null) {
            sprite.spriteGuid = guid == null ? "" : guid;
        }
    }

    /**
     * @return deprecated parent texture GUID when set for migration
     */
    @HostAccess.Export
    public String getTextureGuid() {
        SpriteRendererComponent sprite = component();
        return sprite == null ? "" : sprite.textureGuid;
    }

    /**
     * @param guid deprecated; assigns default sprite for a texture GUID when possible
     */
    @HostAccess.Export
    public void setTextureGuid(String guid) {
        SpriteRendererComponent sprite = component();
        if (sprite != null) {
            sprite.textureGuid = guid == null ? "" : guid;
        }
    }

    /**
     * @return tint color binding
     */
    @HostAccess.Export
    public ColorBinding getColor() {
        return new ColorBinding(
                this::readColor,
                this::writeColor
        );
    }

    /**
     * @return draw order relative to other sprites
     */
    @HostAccess.Export
    public int getSortingOrder() {
        SpriteRendererComponent sprite = component();
        return sprite == null ? 0 : sprite.sortingOrder;
    }

    /**
     * @param value draw order
     */
    @HostAccess.Export
    public void setSortingOrder(int value) {
        SpriteRendererComponent sprite = component();
        if (sprite != null) {
            sprite.sortingOrder = value;
        }
    }

    private SpriteRendererComponent component() {
        return world.getComponent(entity, SpriteRendererComponent.class);
    }

    private float[] readColor() {
        SpriteRendererComponent sprite = component();
        if (sprite == null) {
            return new float[]{1f, 1f, 1f, 1f};
        }
        return new float[]{sprite.r, sprite.g, sprite.b, sprite.a};
    }

    private void writeColor(float r, float g, float b, float a) {
        SpriteRendererComponent sprite = component();
        if (sprite != null) {
            sprite.r = r;
            sprite.g = g;
            sprite.b = b;
            sprite.a = a;
        }
    }
}
