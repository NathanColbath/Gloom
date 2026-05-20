# Physics 2D

Play mode simulates 2D physics with **Box2D** (jbox2d). Attach colliders and rigidbodies in the Inspector; react in scripts with collision and trigger callbacks.

## Components

| Component | Purpose |
|-----------|---------|
| **Rigidbody 2D** | Dynamic/kinematic/static body, mass, gravity scale, velocity |
| **Box Collider 2D** | Rectangle collider |
| **Circle Collider 2D** | Circle collider |
| **Edge Collider 2D** | Segment chain (platforms, walls) |

::: studio-screenshot{file="43-inspector-rigidbody.png"}
Rigidbody 2D and box collider on Player in Inspector.
:::

::: studio-screenshot{file="44-physics-scene-debug.png"}
Scene or game view with collider gizmos visible during play.
:::

## Setup example

1. Add **Rigidbody 2D** (dynamic) to the player.
2. Add **Box Collider 2D** sized to the sprite.
3. Add static colliders on ground objects (no rigidbody, or static body).
4. Press Play — `PhysicsSystem` steps the world after script `fixedUpdate`.

## Triggers

Enable **Is Trigger** on a collider to receive trigger callbacks without solid collision response.

## Script callbacks

```typescript
onCollisionEnter2D(collision: core.Collision2D): void {
  const other = collision.other;
}

onTriggerEnter2D(other: core.Collider2D): void {
  // ...
}
```

## Physics2D API

```typescript
core.Physics2D.setGravity(0, 980);
const hit = core.Physics2D.raycast(ox, oy, dx, dy, distance);
```

See [Scripting API — Physics](scripting-api/physics.md).

## Tips

- Physics runs in play mode only; edit scene colliders are serialized but the world is not stepped while editing.
- Use `fixedUpdate` for forces that must stay in sync with the physics step.

## Related

- [Play mode](play-mode.md)
- [Systems reference](systems-reference.md)
