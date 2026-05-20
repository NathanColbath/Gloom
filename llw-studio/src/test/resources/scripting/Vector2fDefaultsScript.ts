import * as core from "llw.core";

export default class Vector2fDefaultsScript extends core.Script {
  offset = new core.Vector2f(0, 1);
  legacy = core.Vec2.create(2, 3);
}
