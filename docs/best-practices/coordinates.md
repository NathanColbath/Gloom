# Coordinates

LLW uses a single **Y-down** convention across render and math.

## Screen space

- Origin `(0, 0)` is the **top-left** of the window or render target.
- `+X` is right; `+Y` is down.
- Matches SFML, many UI toolkits, and raw mouse pixel coordinates (before camera transform).

```java
sprite.setPosition(100f, 50f);   // 100px right, 50px down from top-left
```

## World space and camera

`Camera2d` maps a world rectangle onto the window. Default setup matches 1:1 pixel mapping at zoom 1.

```java
Camera2d camera = gfx.getCamera();
IntSize size = gfx.getSize();

camera.setCenter(size.width() / 2f, size.height() / 2f);
camera.setSize(size.width(), size.height());
```

### Mouse picking

```java
Vector2f world = camera.screenToWorld(window.mousePosition(), size);
if (hitbox.contains(world.x, world.y)) { /* ... */ }
```

Never use raw mouse pixels for world hit tests when the camera is panned or zoomed.

### Zoom

```java
float zoom = 2f;
camera.setSize(size.width() * zoom, size.height() * zoom);
```

Larger `setSize` values show **more** world units (zoom out). Smaller values zoom in.

## Transforms and origin

Renderables rotate around their **origin** pivot (often sprite center):

```java
sprite.setOrigin(textureWidth / 2f, textureHeight / 2f);
sprite.setPosition(cx, cy);
```

`Transform2f` and `Matrix3x2.fromTransform` apply the same pivot order as SFML.

## Audio vs render Y

Render/math: Y-down. OpenAL listener space: Y-up by convention.

When syncing 3D audio to 2D sprites:

```java
sound.setRelativeToListener(false);
sound.setPosition(spriteX, -spriteY, 0f);
```

For non-spatial SFX, keep `setRelativeToListener(true)` and use `setPan` only.

::: tip One source of truth
Store entity positions in world units (`Vector2f`). Convert at the boundary to screen (`worldToScreen`) or audio (`setPosition`) — avoid maintaining parallel coordinate systems.
:::

## See also

- [Guide — Coordinates](/guide/coordinates)
- [Camera](/render/camera)
- [Scroll Zoom Camera](/cookbook/scroll-zoom-camera)
