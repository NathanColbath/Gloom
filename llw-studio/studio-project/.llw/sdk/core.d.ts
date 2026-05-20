export * from "./index";
export { Vector2f, Vec2, Mathf } from "./math";
export { Color } from "./color";
export { Rect2 } from "./rect";

import type * as CoreTypes from "./index";
import { Vector2f, Vec2, Mathf } from "./math";
import { Color } from "./color";
import { Rect2 } from "./rect";

declare const core: typeof CoreTypes & {
  Vector2f: typeof Vector2f;
  Vec2: typeof Vec2;
  Mathf: typeof Mathf;
  Color: typeof Color;
  Rect2: typeof Rect2;
};

export default core;
