/** Axis-aligned rectangle in Y-down screen space ({@code x}/{@code y} are top-left). */
export declare class Rect2 {
  x: number;
  y: number;
  width: number;
  height: number;
  readonly right: number;
  readonly bottom: number;
  constructor(x?: number, y?: number, width?: number, height?: number);
  contains(x: number, y: number): boolean;
  intersects(other: Rect2): boolean;
  intersection(other: Rect2): Rect2 | null;
  clone(): Rect2;
}
