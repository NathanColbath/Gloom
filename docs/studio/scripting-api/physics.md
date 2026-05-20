# Scripting API — Physics

## Physics2D namespace

```typescript
Physics2D.gravityX: number;
Physics2D.gravityY: number;
Physics2D.setGravity(x: number, y: number): void;

Physics2D.raycast(
  originX, originY,
  directionX, directionY,
  distance: number,
  layerMask?: number
): RaycastHit2D | null;

Physics2D.overlapCircle(
  x, y, radius: number,
  layerMask?: number
): Entity[];
```

## Collision2D (callbacks)

| Field | Description |
|-------|-------------|
| `other` | Other `Entity` or null |
| `relativeVelocityX`, `relativeVelocityY` | Contact velocity |

## Collider2D (triggers)

| Field | Description |
|-------|-------------|
| `entity` | Owning entity |
| `isTrigger` | Trigger vs solid |

## Script callbacks

On `Script` subclass:

- `onCollisionEnter2D` / `Stay` / `Exit`
- `onTriggerEnter2D` / `Stay` / `Exit`

Aliases without `2D` suffix invoke the same handlers.

## Related

- [Physics 2D](../physics-2d.md)
- [Play mode](../play-mode.md)
