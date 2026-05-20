# Scripting API — Components

`getComponent` accepts a component type string or a `Script` subclass constructor.

## ComponentType strings

`SpriteRenderer`, `Tilemap2D`, `Animation2D`, `Camera2D`, `AudioSource`, `Script`, `Rigidbody2D`, `BoxCollider2D`, `CircleCollider2D`, `EdgeCollider2D`, `UILabel`, `UIButton`, `UIToggle`, `UITextField`

## SpriteRendererComponent

| Field | Type |
|-------|------|
| `spriteGuid` | string |
| `color` | Color |
| `sortingOrder` | number |

## Tilemap2DComponent

| Method | Description |
|--------|-------------|
| `getTile(layer, x, y)` | Sprite GUID or null |
| `setTile(layer, x, y, spriteGuid)` | Paint cell |
| `refresh(layer, x, y, radius?)` | Re-run rule tiles nearby |

## Animation2DComponent

| Field / method | Description |
|----------------|-------------|
| `animationGuid` | Animation set asset |
| `defaultState`, `currentState` | State names |
| `play()`, `playState(name)`, `stop()` | Playback |
| `normalizedTime` | Read-only 0–1 |

## Camera2DComponent

Orthographic camera: `orthographicSize`, `depth`, `mainCamera`, world/screen conversion (`worldToScreen`, `screenToWorld`), viewport mouse position when main.

## AudioSourceComponent

`clipGuid`, `volume`, `playOnStart`, `play()`, `stop()`, `playing`.

## Rigidbody2DComponent

`bodyType`, `mass`, `gravityScale`, `velocityX/Y`, `freezeRotation`, `addForce`, `movePosition`.

## Colliders

Box, circle, edge — `isTrigger` for trigger callbacks without solid response.

## ScriptComponentRef

`scriptGuid`, `enabled` — use `getComponent(MyScript)` for the live instance.

## Related

- [Components reference](../components-reference.md)
- [Physics](physics.md)
- [UI](ui.md)
