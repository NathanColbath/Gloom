# Scene and entities

## Scene namespace

```typescript
Scene.findByName(name: string): Entity | null;
Scene.findAllByName(name: string): Entity[];
Scene.findByTag(tag: string): Entity | null;
Scene.findAllByTag(tag: string): Entity[];

Scene.createEntity(): Entity;
Scene.createEntity(name: string): Entity;
Scene.createEntity(template: Entity): Entity;
Scene.createEntity(name: string, prefabPathOrGuid: string): Entity;

Scene.destroyEntity(entity: Entity): void;
```

**Prefab spawn** — pass asset path or GUID as the second argument with a name:

```typescript
const e = Scene.createEntity("Bullet", "Assets/Prefabs/Bullet.prefab.json");
```

## Entity

| Property / method | Description |
|-------------------|-------------|
| `id` | Stable string id |
| `name`, `tag` | Editable; `compareTag(tag)` helper |
| `active` | Active state |
| `parent`, `children` | Hierarchy |
| `transform` | Local transform |
| `worldX`, `worldY` | Cached world position |
| `setParent(parent, worldPositionStays?)` | Reparent |
| `hasComponent` / `getComponent` / `addComponent` / `removeComponent` | ECS access |
| `destroy()` | Remove from scene |

## Transform

| Field | Notes |
|-------|-------|
| `position` | `Vector2` local position |
| `rotation` | **Degrees**, 0 = +X |
| `scale` | `Vector2` |
| `worldPosition` | Read-only world center |
| `translate(dx, dy)` | Offset position |

Use `Math.deg2rad`, `Math.cos` / `Math.sin` or `Math.cosDeg` / `Math.sinDeg` for direction from rotation.

## Related

- [Components](components.md)
- [Prefabs](../prefabs.md)
