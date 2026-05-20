package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * Installs constructible script globals ({@code Vector2f}, {@code Mathf}, {@code Color}, {@code Rect2}).
 */
public final class SdkGlobalsInstaller {
    private SdkGlobalsInstaller() {
    }

    /**
     * Registers {@code Vector2f}, {@code Vec2}, {@code Mathf}, {@code Color}, and {@code Rect2} on {@code globalThis}.
     *
     * @param context Graal JS context
     * @param vec2    vector factory
     * @param mathf   scalar math host
     * @param color   color factory
     * @param rect2   rectangle factory
     */
    public static void install(
            Context context,
            MathBinding.Vec2Factory vec2,
            MathBinding.Mathf mathf,
            ColorMathBinding.ColorFactory color,
            Rect2Binding.Rect2Factory rect2
    ) {
        context.eval("js", """
                (function (vec2, mathf, colorFactory, rect2Factory) {
                  const num = (value, fallback) =>
                    value === undefined || value === null ? fallback : Number(value);

                  const wrapVec = (value) => {
                    const proxy = {
                      get x() { return value.getX(); },
                      set x(v) { value.setX(v); },
                      get y() { return value.getY(); },
                      set y(v) { value.setY(v); },
                      length: () => value.length(),
                      normalize: () => { value.normalize(); return wrapVec(value); },
                      add: (ox, oy) => { value.add(ox, oy); return wrapVec(value); },
                      sub: (ox, oy) => { value.sub(ox, oy); return wrapVec(value); },
                      mul: (scalar) => { value.mul(scalar); return wrapVec(value); },
                      distance: (ox, oy) => value.distance(ox, oy),
                      distanceTo: (other) => value.distanceTo(hostVec(other)),
                      dot: (other) => value.dot(hostVec(other)),
                      lerp: (ox, oy, t) => { value.lerp(ox, oy, t); return wrapVec(value); },
                      clone: () => wrapVec(value.clone()),
                      equals: (other) => value.equals(hostVec(other)),
                      toString: () => value.toString(),
                    };
                    Object.defineProperty(proxy, "__host", { value, enumerable: false });
                    return proxy;
                  };

                  const hostVec = (v) => (v && v.__host) ? v.__host : v;

                  class Vector2f {
                    constructor(x, y) {
                      return wrapVec(vec2.create(num(x, 0), num(y, 0)));
                    }
                    static zero() { return wrapVec(vec2.zero()); }
                    static one() { return wrapVec(vec2.one()); }
                    static create(x, y) { return new Vector2f(x, y); }
                    static dot(a, b) { return vec2.dot(hostVec(a), hostVec(b)); }
                    static distance(a, b) { return vec2.distance(hostVec(a), hostVec(b)); }
                    static lerp(a, b, t) { return wrapVec(vec2.lerp(hostVec(a), hostVec(b), t)); }
                    static fromAngle(radians, length) {
                      return wrapVec(vec2.fromAngle(radians, length === undefined ? 0 : length));
                    }
                    static fromAngleDegrees(degrees, length) {
                      return wrapVec(vec2.fromAngleDegrees(degrees, length === undefined ? 0 : length));
                    }
                  }

                  const wrapColor = (value) => {
                    const proxy = {
                      get r() { return value.getR(); },
                      set r(v) { value.setR(v); },
                      get g() { return value.getG(); },
                      set g(v) { value.setG(v); },
                      get b() { return value.getB(); },
                      set b(v) { value.setB(v); },
                      get a() { return value.getA(); },
                      set a(v) { value.setA(v); },
                      clone: () => wrapColor(value.clone()),
                      lerp: (other, t) => wrapColor(value.lerp(hostColor(other), t)),
                      multiply: (scalar) => { value.multiply(scalar); return wrapColor(value); },
                    };
                    Object.defineProperty(proxy, "__host", { value, enumerable: false });
                    return proxy;
                  };

                  const hostColor = (v) => (v && v.__host) ? v.__host : v;

                  class Color {
                    constructor(r, g, b, a) {
                      return wrapColor(colorFactory.create(
                        num(r, 1), num(g, 1), num(b, 1), num(a, 1)));
                    }
                  }

                  const wrapRect = (value) => {
                    const proxy = {
                      get x() { return value.x; },
                      set x(v) { value.x = v; },
                      get y() { return value.y; },
                      set y(v) { value.y = v; },
                      get width() { return value.width; },
                      set width(v) { value.width = v; },
                      get height() { return value.height; },
                      set height(v) { value.height = v; },
                      get right() { return value.getRight(); },
                      get bottom() { return value.getBottom(); },
                      contains: (px, py) => value.contains(px, py),
                      intersects: (other) => value.intersects(hostRect(other)),
                      intersection: (other) => {
                        const hit = value.intersection(hostRect(other));
                        return hit == null ? null : wrapRect(hit);
                      },
                      clone: () => wrapRect(value.clone()),
                    };
                    Object.defineProperty(proxy, "__host", { value, enumerable: false });
                    return proxy;
                  };

                  const hostRect = (v) => (v && v.__host) ? v.__host : v;

                  class Rect2 {
                    constructor(x, y, width, height) {
                      return wrapRect(rect2Factory.create(
                        num(x, 0), num(y, 0), num(width, 0), num(height, 0)));
                    }
                  }

                  class Mathf {
                    static clamp(value, min, max) { return mathf.clamp(value, min, max); }
                    static lerp(a, b, t) { return mathf.lerp(a, b, t); }
                    static inverseLerp(a, b, value) { return mathf.inverseLerp(a, b, value); }
                    static smoothstep(t) { return mathf.smoothstep(t); }
                    static min(a, b) { return mathf.min(a, b); }
                    static max(a, b) { return mathf.max(a, b); }
                    static abs(value) { return mathf.abs(value); }
                    static round(value) { return mathf.round(value); }
                    static floor(value) { return mathf.floor(value); }
                    static ceil(value) { return mathf.ceil(value); }
                    static sin(value) { return mathf.sin(value); }
                    static cos(value) { return mathf.cos(value); }
                    static cosDeg(degrees) { return mathf.cosDeg(degrees); }
                    static sinDeg(degrees) { return mathf.sinDeg(degrees); }
                    static atan2(y, x) { return mathf.atan2(y, x); }
                    static sqrt(value) { return mathf.sqrt(value); }
                    static deg2rad(degrees) { return mathf.deg2rad(degrees); }
                    static rad2deg(radians) { return mathf.rad2deg(radians); }
                    static random() { return mathf.random(); }
                    static randomRange(min, max) { return mathf.randomRange(min, max); }
                  }

                  globalThis.Vector2f = Vector2f;
                  globalThis.Vec2 = Vector2f;
                  globalThis.Mathf = Mathf;
                  globalThis.Color = Color;
                  globalThis.Rect2 = Rect2;
                })
                """).execute(
                Value.asValue(vec2),
                Value.asValue(mathf),
                Value.asValue(color),
                Value.asValue(rect2)
        );
    }
}
