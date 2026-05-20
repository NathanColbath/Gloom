/** Mutable 2D vector from {@link Vector2f} and related helpers. */
export declare class Vector2f {
  x: number;
  y: number;
  constructor(x?: number, y?: number);
  length(): number;
  /** Scales this vector to unit length when non-zero. Returns {@code this}. */
  normalize(): Vector2f;
  /** Adds to this vector in place. Returns {@code this}. */
  add(x: number, y: number): Vector2f;
  /** Subtracts from this vector in place. Returns {@code this}. */
  sub(x: number, y: number): Vector2f;
  /** Multiplies this vector by a scalar in place. Returns {@code this}. */
  mul(scalar: number): Vector2f;
  distance(x: number, y: number): number;
  distanceTo(other: Vector2f): number;
  dot(other: Vector2f): number;
  /** Linearly interpolates this vector toward {@code (x, y)} in place. Returns {@code this}. */
  lerp(x: number, y: number, t: number): Vector2f;
  /** Returns a new vector with the same components. */
  clone(): Vector2f;
  equals(other: Vector2f): boolean;
  toString(): string;
  static zero(): Vector2f;
  static one(): Vector2f;
  static create(x?: number, y?: number): Vector2f;
  static dot(a: Vector2f, b: Vector2f): number;
  static distance(a: Vector2f, b: Vector2f): number;
  static lerp(a: Vector2f, b: Vector2f, t: number): Vector2f;
  /** @param radians angle in radians */
  static fromAngle(radians: number, length?: number): Vector2f;
  /** Direction from {@link Transform.rotation} (degrees; +X at 0°). */
  static fromAngleDegrees(degrees: number, length?: number): Vector2f;
}

export type Vector2 = Vector2f;
/** @deprecated Use {@link Vector2f} */
export type Vec2 = Vector2f;
/** @deprecated Use {@link Vector2f} */
export declare const Vec2: typeof Vector2f;

/** Scalar math functions (Unity-style static class). */
export declare class Mathf {
  private constructor();
  static clamp(value: number, min: number, max: number): number;
  static lerp(a: number, b: number, t: number): number;
  static inverseLerp(a: number, b: number, value: number): number;
  static smoothstep(t: number): number;
  static min(a: number, b: number): number;
  static max(a: number, b: number): number;
  static abs(value: number): number;
  /** @param value angle in radians */
  static sin(value: number): number;
  /** @param value angle in radians */
  static cos(value: number): number;
  /** @param degrees angle in degrees (e.g. {@link Transform.rotation}) */
  static sinDeg(degrees: number): number;
  /** @param degrees angle in degrees (e.g. {@link Transform.rotation}) */
  static cosDeg(degrees: number): number;
  static atan2(y: number, x: number): number;
  static sqrt(value: number): number;
  static deg2rad(degrees: number): number;
  static rad2deg(radians: number): number;
  /** @return value in {@code [0, 1)} */
  static random(): number;
  static randomRange(min: number, max: number): number;
}

export declare namespace Math {
  function clamp(value: number, min: number, max: number): number;
  function lerp(a: number, b: number, t: number): number;
  function inverseLerp(a: number, b: number, value: number): number;
  function smoothstep(t: number): number;
  function min(a: number, b: number): number;
  function max(a: number, b: number): number;
  function abs(value: number): number;
  /** @param value angle in radians */
  function sin(value: number): number;
  /** @param value angle in radians */
  function cos(value: number): number;
  /** @param degrees angle in degrees (e.g. {@link Transform.rotation}) */
  function sinDeg(degrees: number): number;
  /** @param degrees angle in degrees (e.g. {@link Transform.rotation}) */
  function cosDeg(degrees: number): number;
  function atan2(y: number, x: number): number;
  function sqrt(value: number): number;
  function deg2rad(degrees: number): number;
  function rad2deg(radians: number): number;
  /** @return value in {@code [0, 1)} */
  function random(): number;
  function randomRange(min: number, max: number): number;
}
