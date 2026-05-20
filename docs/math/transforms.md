# Transforms

2D transforms combine translation, rotation, scale, and origin pivot — matching SFML `Transformable` semantics.

## In this section

- [Matrix](/math/matrix) — `Matrix3x2` composition and GL upload
- [Transformable](/render/transformable) — render-side API
- [Camera](/render/camera) — view/projection matrices

## Transform2f

Groups position, rotation, scale, and origin pivot.

```java
import org.llw.math.transform.Transform2f;

Transform2f xf = new Transform2f();
xf.setPosition(100f, 200f);
xf.setRotation((float) Math.toRadians(45));
xf.setScale(2f, 2f);
xf.setOrigin(16f, 16f);

Matrix3x2 matrix = xf.toMatrix();
```

`AbstractTransformable` in the render module delegates to `Transform2f` internally — keep simulation transforms in math, copy to renderables each frame or share values.

Transform order when building the matrix: **translate → rotate → scale**, with origin subtracted before rotate/scale and added back (SFML-style).

## Matrix3x2

2D affine transform stored as 4×4 column-major for OpenGL.

```java
import org.llw.math.matrix.Matrix3x2;

Matrix3x2 m = new Matrix3x2();
m.translate(10f, 20f).rotate(0.5f).scale(2f, 2f);

Matrix3x2 ortho = Matrix3x2.ortho(left, right, worldTop, worldBottom); // Y-down
Vector2f pos = new Vector2f(100f, 200f);
Vector2f scale = new Vector2f(2f, 2f);
Vector2f origin = new Vector2f(16f, 16f);
Matrix3x2 view = Matrix3x2.fromTransform(pos, rotationRad, scale, origin);
```

See [Matrix](/math/matrix) for composition and GL upload.

## Math utilities

```java
import org.llw.math.util.Angle;
import org.llw.math.util.MathUtils;

float t = MathUtils.inverseLerp(0f, 100f, 50f);   // 0.5
float v = MathUtils.remap(0.5f, 0f, 1f, 0f, 255f);
float eased = MathUtils.smoothstep(0.3f);
float rad = Angle.toRadians(90f);
float deg = Angle.toDegrees((float) Math.PI);
```

::: tip One matrix scratch
Reuse a single `Matrix3x2` in entity loops via `identity().translate(...)` rather than allocating per sprite.
:::

## See also

- [Vectors](/math/vectors)
- [Renderables](/render/renderables)
- [Tutorial — Transforms](/tutorials/07-transforms)
