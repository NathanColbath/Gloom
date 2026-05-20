import * as core from "llw.core";

export default class Bullet extends core.Script {
  moveAngle = 0;
  moveSpeed = 10;
  dieTime = 5;

  start(): void {
    core.Logger.log("" + this.moveAngle);
  }

  update(): void {
    this.dieTime -= core.Time.deltaTime;

    const step = this.moveSpeed;

    this.transform.position.x += core.Math.cos(this.moveAngle) * step;
    this.transform.position.y += core.Math.sin(this.moveAngle) * step;

    this.transform.rotation = this.moveAngle;

    if (this.dieTime <= 0) {
      core.Scene.destroyEntity(this.entity);
    }
  }
}
