import * as core from "llw.core";

export default class PlayerController extends core.Script {
  moveSpeed = 0;
  acceleration = 80;
  rotationSpeed = 120;

  /** Facing direction; not exposed in the Inspector. */
  private rb = this.entity.requireComponent<core.Rigidbody2DComponent>("Rigidbody2D");

  start(): void {
      
  }

  update(): void {
    let vx = 0;
    if (core.Input.getKey(core.Keys.VK_A)) vx -= this.moveSpeed;
    if (core.Input.getKey(core.Keys.VK_D)) vx += this.moveSpeed;
    // Keep current vertical velocity (gravity/jumps)
    this.rb.setVelocity(vx, this.rb.velocityY);
  }
}
