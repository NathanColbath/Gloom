# Components reference

All built-in components registered in `ComponentCatalog`. **Addable** components appear in **Add Component**; core components are always present or header-only.

## Core

| Component | Addable | Key fields |
|-----------|---------|------------|
| Transform 2D | No (always) | position, rotation (deg), scale |
| Active | No (header) | active flag |

## Rendering

| Component | Addable | Key fields |
|-----------|---------|------------|
| Sprite Renderer | Yes | spriteGuid, color, sortingOrder |
| Tilemap | Yes | tilesetTextureGuid, layer tile data |
| Camera 2D | Yes | mainCamera, orthographicSize, depth |

## Animation

| Component | Addable | Key fields |
|-----------|---------|------------|
| Animation 2D | Yes | animationGuid, defaultState, speed, loop, playOnStart |

## Audio

| Component | Addable | Key fields |
|-----------|---------|------------|
| Audio Source | Yes | clipGuid, volume, playOnStart |

## Scripting

| Component | Addable | Key fields |
|-----------|---------|------------|
| Script | Yes | script class/GUID, attachments, serialized TS fields |

## Physics

| Component | Addable | Key fields |
|-----------|---------|------------|
| Rigidbody 2D | Yes | bodyType, mass, gravityScale, velocity, freezeRotation |
| Box Collider 2D | Yes | size, offset, isTrigger |
| Circle Collider 2D | Yes | radius, offset, isTrigger |
| Edge Collider 2D | Yes | points, isTrigger |

## UI

| Component | Addable | Key fields |
|-----------|---------|------------|
| UI Canvas | Yes | sortingOrder, enabled |
| UI Label | Yes | text |
| UI Button | Yes | label, interactable |
| UI Toggle | Yes | label, isOn, interactable |
| UI Text Field | Yes | value, interactable |

## Script access

TypeScript uses string literals or typed getters:

```typescript
entity.getComponent("SpriteRenderer");
entity.getComponent("Rigidbody2D");
```

Full typings: [Scripting API — Components](scripting-api/components.md).

## Related

- [Inspector](inspector.md)
- [ECS and GameObjects](ecs-and-gameobjects.md)
