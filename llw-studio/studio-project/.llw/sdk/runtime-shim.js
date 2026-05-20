// Runtime entry for llw.core — forwards to Graal host globals installed at play time.

function resolveHostMember(host, prop) {
  if (host == null || typeof prop !== "string") {
    return undefined;
  }
  const direct = host[prop];
  if (direct !== undefined) {
    return typeof direct === "function" ? direct.bind(host) : direct;
  }
  const getter = "get" + prop.charAt(0).toUpperCase() + prop.slice(1);
  if (typeof host[getter] === "function") {
    return host[getter]();
  }
  return undefined;
}

function hostBinding(globalName) {
  return new Proxy(
    {},
    {
      get(_target, prop) {
        return resolveHostMember(globalThis[globalName], prop);
      },
    }
  );
}

function llwScriptBase() {
  return globalThis.LLW?.Script ?? class Script {};
}

export class Script extends llwScriptBase() {}

export const Time = hostBinding("Time");
export const Input = hostBinding("Input");
export const Logger = hostBinding("Logger");
export const Math = hostBinding("Math");
export const Camera = hostBinding("Camera");
export const Scene = hostBinding("Scene");
export const Assets = hostBinding("Assets");
export { Keys } from "./keys.js";

class FallbackVector2f {
  constructor(x = 0, y = 0) {
    this.x = Number(x);
    this.y = Number(y);
  }
  length() {
    return Math.hypot(this.x, this.y);
  }
  normalize() {
    const len = this.length();
    if (len > 1e-6) {
      this.x /= len;
      this.y /= len;
    }
    return this;
  }
  add(x, y) {
    this.x += x;
    this.y += y;
    return this;
  }
  sub(x, y) {
    this.x -= x;
    this.y -= y;
    return this;
  }
  mul(scalar) {
    this.x *= scalar;
    this.y *= scalar;
    return this;
  }
  distance(x, y) {
    return Math.hypot(this.x - x, this.y - y);
  }
  distanceTo(other) {
    return this.distance(other.x, other.y);
  }
  dot(other) {
    return this.x * other.x + this.y * other.y;
  }
  lerp(x, y, t) {
    this.x += (x - this.x) * t;
    this.y += (y - this.y) * t;
    return this;
  }
  clone() {
    return new FallbackVector2f(this.x, this.y);
  }
  equals(other) {
    return this.x === other.x && this.y === other.y;
  }
  toString() {
    return `Vector2f(${this.x}, ${this.y})`;
  }
  static zero() {
    return new FallbackVector2f(0, 0);
  }
  static one() {
    return new FallbackVector2f(1, 1);
  }
  static create(x, y) {
    return new FallbackVector2f(x, y);
  }
  static dot(a, b) {
    return a.x * b.x + a.y * b.y;
  }
  static distance(a, b) {
    return a.distanceTo(b);
  }
  static lerp(a, b, t) {
    return new FallbackVector2f(
      a.x + (b.x - a.x) * t,
      a.y + (b.y - a.y) * t
    );
  }
  static fromAngle(radians, length = 1) {
    const len = length <= 0 ? 1 : length;
    return new FallbackVector2f(Math.cos(radians) * len, Math.sin(radians) * len);
  }
  static fromAngleDegrees(degrees, length = 1) {
    return Vector2f.fromAngle(FallbackMathf.deg2rad(degrees), length);
  }
}

class FallbackMathf {
  static clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
  }
  static lerp(a, b, t) {
    return a + (b - a) * t;
  }
  static inverseLerp(a, b, value) {
    return a === b ? 0 : (value - a) / (b - a);
  }
  static smoothstep(t) {
    const x = FallbackMathf.clamp(t, 0, 1);
    return x * x * (3 - 2 * x);
  }
  static min(a, b) {
    return Math.min(a, b);
  }
  static max(a, b) {
    return Math.max(a, b);
  }
  static abs(value) {
    return Math.abs(value);
  }
  static sin(value) {
    return Math.sin(value);
  }
  static cos(value) {
    return Math.cos(value);
  }
  static cosDeg(degrees) {
    return Math.cos(FallbackMathf.deg2rad(degrees));
  }
  static sinDeg(degrees) {
    return Math.sin(FallbackMathf.deg2rad(degrees));
  }
  static atan2(y, x) {
    return Math.atan2(y, x);
  }
  static sqrt(value) {
    return Math.sqrt(value);
  }
  static deg2rad(degrees) {
    return (degrees * Math.PI) / 180;
  }
  static rad2deg(radians) {
    return (radians * 180) / Math.PI;
  }
  static random() {
    return Math.random();
  }
  static randomRange(min, max) {
    return min + Math.random() * (max - min);
  }
}

class FallbackColor {
  constructor(r = 1, g = 1, b = 1, a = 1) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
  clone() {
    return new FallbackColor(this.r, this.g, this.b, this.a);
  }
  lerp(other, t) {
    return new FallbackColor(
      this.r + (other.r - this.r) * t,
      this.g + (other.g - this.g) * t,
      this.b + (other.b - this.b) * t,
      this.a + (other.a - this.a) * t
    );
  }
  multiply(scalar) {
    this.r *= scalar;
    this.g *= scalar;
    this.b *= scalar;
    this.a *= scalar;
    return this;
  }
}

class FallbackRect2 {
  constructor(x = 0, y = 0, width = 0, height = 0) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  get right() {
    return this.x + this.width;
  }
  get bottom() {
    return this.y + this.height;
  }
  contains(px, py) {
    return px >= this.x && px <= this.right && py >= this.y && py <= this.bottom;
  }
  intersects(other) {
    return (
      this.x < other.right &&
      this.right > other.x &&
      this.y < other.bottom &&
      this.bottom > other.y
    );
  }
  intersection(other) {
    const left = Math.max(this.x, other.x);
    const top = Math.max(this.y, other.y);
    const right = Math.min(this.right, other.right);
    const bottom = Math.min(this.bottom, other.bottom);
    if (left >= right || top >= bottom) {
      return null;
    }
    return new FallbackRect2(left, top, right - left, bottom - top);
  }
  clone() {
    return new FallbackRect2(this.x, this.y, this.width, this.height);
  }
}

function hostClass(name, fallback) {
  const host = globalThis[name];
  return typeof host === "function" ? host : fallback;
}

export const Vector2f = hostClass("Vector2f", FallbackVector2f);
export const Vec2 = Vector2f;
export const Mathf = hostClass("Mathf", FallbackMathf);
export const Color = hostClass("Color", FallbackColor);
export const Rect2 = hostClass("Rect2", FallbackRect2);
