import * as core from "llw.core";

export default class PlayerController extends core.Script {
  moveSpeed = 0;
  acceleration = 80;
  rotationSpeed = 120;

  /** Facing direction; not exposed in the Inspector. */
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
