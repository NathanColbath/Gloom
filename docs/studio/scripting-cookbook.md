# Scripting cookbook

Recipes using scripts from `llw-studio/studio-project`.

## Player movement and rotation

`Assets/Scripts/PlayerController.ts` — accelerate while holding W, rotate with A/D, move along facing vector.

```typescript
import * as core from "llw.core";

export default class PlayerController extends core.Script {
  moveSpeed = 0;
  acceleration = 80;
  rotationSpeed = 120;

  private moveVector = new core.Vector2f(0, 1);

  update(): void {
    if (core.Input.getKey(core.Keys.VK_W)) {
      this.moveSpeed += this.acceleration * core.Time.deltaTime;
      const rad = core.Math.deg2rad(this.transform.rotation);
      this.moveVector.x = core.Math.cos(rad);
      this.moveVector.y = core.Math.sin(rad);
      this.moveVector.normalize();
    }
    if (core.Input.getKey(core.Keys.VK_A)) {
      this.transform.rotation -= this.rotationSpeed * core.Time.deltaTime;
    } else if (core.Input.getKey(core.Keys.VK_D)) {
      this.transform.rotation += this.rotationSpeed * core.Time.deltaTime;
    }
    const step = this.moveVector.mul(this.moveSpeed * core.Time.deltaTime);
    this.transform.position.x += step.x;
    this.transform.position.y += step.y;
  }
}
```

Tune `acceleration` in the Inspector without recompiling field layout (refresh scripts after code changes to fields).

## Camera follow

`CameraController.ts` — lerp toward a target entity assigned in the Inspector (`Entity | null` field pattern).

## Bullet prefab

`Bullet.ts` — spawn from prefab, move along angle, destroy after lifetime:

```typescript
export default class Bullet extends core.Script {
  moveAngle = 0;
  moveSpeed = 10;
  dieTime = 5;

  update(): void {
    this.dieTime -= core.Time.deltaTime;
    this.transform.position.x += core.Math.cos(this.moveAngle) * this.moveSpeed;
    this.transform.position.y += core.Math.sin(this.moveAngle) * this.moveSpeed;
    this.transform.rotation = this.moveAngle;
    if (this.dieTime <= 0) {
      core.Scene.destroyEntity(this.entity);
    }
  }
}
```

Wire `moveAngle` when spawning from a spawner script or prefab instance fields.

## Find objects by name or tag

```typescript
const player = core.Scene.findByName("Player");
const enemies = core.Scene.findAllByTag("Enemy");
```

## Physics collision

```typescript
onCollisionEnter2D(collision: core.Collision2D): void {
  if (collision.other?.compareTag("Enemy")) {
    this.entity.destroy();
  }
}
```

## UI button

```typescript
const btn = this.entity.getComponent("UIButton");
if (btn?.clicked) {
  core.Logger.log("Start pressed");
}
```

## Related

- [Scripting](scripting.md)
- [Scripting API overview](scripting-api/overview.md)
- [Prefabs](prefabs.md)
