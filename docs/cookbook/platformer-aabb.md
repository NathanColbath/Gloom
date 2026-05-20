# Platformer AABB

## Problem

Side-scrollers need fast, reliable collision between a player and solid tiles or platforms. Rotated polygons are overkill for axis-aligned level geometry; you need cheap overlap tests that respect LLW's Y-down coordinate system (larger Y is lower on screen).

## Solution

Represent the player and each platform as `org.llw.math.geometry.Aabb2f` boxes. Use `overlaps` for broad-phase contact and `Intersection2.intersects` when you prefer the static helper style. Resolve vertical collisions by separating on the Y axis after detecting overlap.

```java
import org.llw.math.collision.Intersection2;
import org.llw.math.geometry.Aabb2f;

Aabb2f player = Aabb2f.fromCenterExtents(100f, 200f, 16f, 24f); // half-width, half-height

List<Aabb2f> platforms = List.of(
    new Aabb2f(0f, 300f, 400f, 320f),   // minX, minY, maxX, maxY
    new Aabb2f(200f, 250f, 500f, 270f)
);

float playerVx = 0f;
float playerVy = 0f;
final float gravity = 900f;
final float moveSpeed = 200f;

boolean onGround = false;

void updatePlayer(float dt) {
    if (window.isKeyDown(Key.A)) {
        playerVx = -moveSpeed;
    } else if (window.isKeyDown(Key.D)) {
        playerVx = moveSpeed;
    } else {
        playerVx = 0f;
    }

    if (onGround && window.isKeyDown(Key.SPACE)) {
        playerVy = -420f; // jump: negative Y is up in Y-down space
    }

    playerVy += gravity * dt;

    // Move X then resolve
    player.minX += playerVx * dt;
    player.maxX += playerVx * dt;
    for (Aabb2f tile : platforms) {
        if (player.overlaps(tile)) {
            if (playerVx > 0f) {
                player.maxX = tile.minX;
                player.minX = player.maxX - 32f;
            } else if (playerVx < 0f) {
                player.minX = tile.maxX;
                player.maxX = player.minX + 32f;
            }
            playerVx = 0f;
        }
    }

    // Move Y then resolve
    player.minY += playerVy * dt;
    player.maxY += playerVy * dt;
    onGround = false;
    for (Aabb2f tile : platforms) {
        if (Intersection2.intersects(player, tile)) {
            if (playerVy > 0f) {
                // Falling — land on top of tile
                player.maxY = tile.minY;
                player.minY = player.maxY - 48f;
                playerVy = 0f;
                onGround = true;
            } else if (playerVy < 0f) {
                // Rising — hit ceiling
                player.minY = tile.maxY;
                player.maxY = player.minY + 48f;
                playerVy = 0f;
            }
        }
    }
}
```

Build boxes from sprite bounds:

```java
float w = 32f, h = 48f;
float cx = sprite.getPosition().x;
float cy = sprite.getPosition().y;
Aabb2f bounds = Aabb2f.fromCenterExtents(cx, cy, w / 2f, h / 2f);
```

Convert to `RectF` for drawing debug outlines:

```java
RectF debugRect = player.toRect();
```

::: details Variations

- **Tile grid:** Store platforms in a 2D array and only test tiles near the player using grid cell indices — same `Aabb2f` math, fewer pairs.
- **One-way platforms:** On Y resolution, land only if the player's previous `maxY` was above `tile.minY` (passed through from last frame).
- **Moving platforms:** Update tile `minX`/`maxX` each frame before resolution; add platform delta to player position on landing.
- **Fixed timestep:** Run this resolver inside the fixed `FIXED_DT` loop from [Fixed Timestep](/cookbook/fixed-timestep) for stable jumps.

:::

## Pitfalls

- **Y-down gravity:** Gravity should **increase** `playerVy` (positive), and jumping uses **negative** `playerVy`. Inverting this launches the player toward the top of the screen incorrectly.
- **Separating both axes in one pass:** Move and resolve X, then move and resolve Y. Simultaneous diagonal correction can snag on corners.
- **Hard-coded half extents in resolution:** Keep width/height in constants shared between `fromCenterExtents` and separation math so resize does not desync.
- **Touching edges:** `overlaps` uses strict `<` on max/min pairs; edge-aligned resting is stable but shared edges between two tiles can confuse one-way logic — add small epsilon if needed.

## See also

- [AABB](/math/aabb)
- [Collision](/math/collision)
- [Entity Lite](/cookbook/entity-component-lite)
- [Fixed Timestep](/cookbook/fixed-timestep)
