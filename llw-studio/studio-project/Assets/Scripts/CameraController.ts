import * as core from "llw.core";
import type { Entity } from "llw.core";

export default class CameraController extends core.Script {
  target: Entity | undefined;
  lerpSpeed = 0.5;

  update(): void {
    if (this.target != null) {
      const lx = core.Math.lerp(
        this.transform.position.x,
        this.target.transform.position.x,
        this.lerpSpeed
      );
      const ly = core.Math.lerp(
        this.transform.position.y,
        this.target.transform.position.y,
        this.lerpSpeed
      );
      this.transform.position.x = lx;
      this.transform.position.y = ly;
    }
  }
}
