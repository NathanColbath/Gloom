# Scripting with TypeScript

Gameplay logic is written in **TypeScript**, bundled with **esbuild**, and executed in play mode by **GraalJS**. Scripts extend `core.Script` and attach to entities via the **Script** component.

## Create a script

1. **Assets → Create Script** (or Project context menu).
2. Enter a class name; Studio creates `Assets/Scripts/YourName.ts` from a template.
3. Attach the script on an entity: **Add Component → Script**, pick the class.

::: studio-screenshot{file="32-create-script-dialog.png"}
Create Script dialog with class name field.
:::

## Project tooling

On project open, Studio generates:

- `package.json` / `tsconfig.json` pointing at `.llw/sdk/`
- Copied SDK under `.llw/sdk/` (`llw.core` module)

Open the project folder in VS Code or Cursor for IntelliSense.

::: studio-screenshot{file="24-script-ide.png"}
External IDE with PlayerController.ts and llw.core types.
:::

## Script shape

```typescript
import * as core from "llw.core";

export default class PlayerController extends core.Script {
  moveSpeed = 0;
  acceleration = 80;

  private moveVector = new core.Vector2f(0, 1);

  update(): void {
    if (core.Input.getKey(core.Keys.VK_W)) {
      this.moveSpeed += this.acceleration * core.Time.deltaTime;
    }
    // ...
  }
}
```

## Inspector-exposed fields

Public instance fields on your script class appear in the Inspector and serialize on the entity:

| Type | Supported |
|------|-----------|
| `number`, `boolean`, `string` | Yes |
| `Vector2` / `Vector2f` | Yes |
| `Entity \| null` | Yes |

**Not** exposed: `private`, `protected`, `static`, `#privateField`, or inherited members (`entity`, `transform`, `enabled`).

Use private fields for caches and timers ([`index.d.ts`](../../llw-studio/src/main/resources/scripting-sdk/index.d.ts)).

::: studio-screenshot{file="07-inspector-script.png"}
Script fields edited per entity instance in Inspector.
:::

## Lifecycle callbacks

| Callback | When |
|----------|------|
| `start()` | Once after instance creation |
| `update()` | Each frame |
| `fixedUpdate()` | Fixed timestep (physics cadence) |
| `onEnable()` / `onDisable()` | Active toggled |
| `onDestroy()` | Entity destroyed or play stopped |
| `onCollisionEnter2D` / `Stay` / `Exit` | Physics contacts |
| `onTriggerEnter2D` / `Stay` / `Exit` | Trigger colliders |

Aliases without `2D` suffix call the same handlers.

## Refresh and cache

- **Assets → Refresh Scripts** — re-runs esbuild and schema extraction.
- Output: `.studio/metadata/script-cache/` (per script GUID).
- Schemas: `.studio/metadata/script-schemas/` for Inspector layout.

During play, file watcher changes are deferred until Stop.

## Play-mode globals

`ScriptHostApi` installs Graal bindings for `Scene`, `Assets`, `Math`, `Time`, `Input`, `Camera`, `Physics2D`, `Logger`, and `console`. Import style in source is `llw.core`; see [Scripting API overview](scripting-api/overview.md).

## Errors

Bundle failures and runtime exceptions appear in the [Console](console.md).

::: studio-screenshot{file="17-console-compile-error.png"}
Console showing esbuild compile error.
:::

## Related

- [Scripting cookbook](scripting-cookbook.md)
- [Play mode](play-mode.md)
- [Physics 2D](physics-2d.md)
