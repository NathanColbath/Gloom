import type { Entity } from "./index";

export interface CameraPoint {
  readonly x: number;
  readonly y: number;
}

export declare namespace Camera {
  const active: boolean;
  const main: Entity | null;
}
