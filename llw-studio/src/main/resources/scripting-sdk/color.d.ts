/** Mutable RGBA color from {@link Color} constructor and helpers. */
export declare class Color {
  r: number;
  g: number;
  b: number;
  a: number;
  constructor(r?: number, g?: number, b?: number, a?: number);
  clone(): Color;
  lerp(other: Color, t: number): Color;
  /** Multiplies RGBA by a scalar in place. Returns {@code this}. */
  multiply(scalar: number): Color;
}
