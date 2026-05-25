# Scripting API overview

Play-mode scripts import **`llw.core`**, which re-exports the studio TypeScript SDK. GraalJS executes bundled output; `ScriptHostApi` exposes globals at runtime.

## Import

```typescript
import * as core from "llw.core";
```

## Script base class

```typescript
export default class MyBehaviour extends core.Script {
  start?(): void;
  update?(): void;
  fixedUpdate?(): void;
  // collision / trigger callbacks ...
}
```

Members on every instance:

| Member | Description |
|--------|-------------|
| `entity` | This `Entity` |
| `transform` | `Transform` shortcut |
| `enabled` | When false, updates skipped |

## Global namespaces

| API | Purpose |
|-----|---------|
| `Time` | `deltaTime`, `time`, `frameCount` |
| `Input` | Keys, mouse buttons, position, scroll |
| `Logger` / `console` | Log to Studio Console |
| `Physics2D` | Gravity, raycast, overlap |
| `Scene` | Find/create/destroy entities |
| `Assets` | GUID lookup, animation metadata |

Re-exports: `Vector2f`, `Vec2`, `Mathf`, `Math`, `Color`, `Rect2`, `Keys`.

## Components on entities

```typescript
const sprite = this.entity.requireComponent("SpriteRenderer");
const rb = this.entity.requireComponent("Rigidbody2D");
const script = this.entity.getComponent(MyOtherScript); // optional: null if missing
```

## Sub-guides

- [Scene and entities](scene-and-entities.md)
- [Components](components.md)
- [Physics](physics.md)
- [Animation](animation.md)
- [UI](ui.md)
- [Input and keys](input-and-keys.md)

## SDK source

Types live in `llw-studio/src/main/resources/scripting-sdk/` and copy to `.llw/sdk/` in your project.

## Related

- [Scripting](../scripting.md)
- [Scripting cookbook](../scripting-cookbook.md)
