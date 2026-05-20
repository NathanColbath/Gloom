import * as core from "llw.core";
import type { Collision2D } from "llw.core";
import Bullet from "./Bullet";

export default class Test extends core.Script {
  onCollisionEnter2D(collision: Collision2D): void {
    core.Logger.log("Collide: " + collision.other?.name);

    const bullet = collision.other?.getComponent(Bullet);
    if (bullet != null) {
      core.Logger.log("HAS BULLET COMPONENT");
    }

    if (collision.other?.name == "Bullet") {
      core.Scene.destroyEntity(collision.other);
      core.Scene.destroyEntity(this.entity);
    }
  }
}
