export interface Collision2D {
  readonly other: import("./index").Entity | null;
  readonly relativeVelocityX: number;
  readonly relativeVelocityY: number;
}

export interface Collider2D {
  readonly entity: import("./index").Entity | null;
  readonly isTrigger: boolean;
}

export interface RaycastHit2D {
  readonly pointX: number;
  readonly pointY: number;
  readonly fraction: number;
}

export interface Physics2DNamespace {
  gravityX: number;
  gravityY: number;
  setGravity(x: number, y: number): void;
  raycast(
    originX: number,
    originY: number,
    directionX: number,
    directionY: number,
    distance: number,
    layerMask?: number
  ): RaycastHit2D | null;
  overlapCircle(x: number, y: number, radius: number, layerMask?: number): import("./index").Entity[];
}
