# Matrix

`Matrix3x2` in `org.llw.math.matrix` is a mutable 2D affine transform stored as a **4×4 column-major** matrix for OpenGL.

Despite the name, the type is a full 4×4 matrix with Z/W identity — matching how `GraphicsContext` uploads uniforms.

## Identity and composition

```java
import org.llw.math.matrix.Matrix3x2;

Matrix3x2 m = new Matrix3x2();
m.identity()
 .translate(100f, 50f)
 .rotate((float) Math.toRadians(30))
 .scale(2f, 2f);
```

Operations **post-multiply** (apply new transform after existing):

```
result = previous × newTransform
```

Order matters: `translate` then `rotate` moves the origin before rotating.

## Point transform

Build matrices with `fromTransform` or chain `translate` / `rotate` / `scale`, then multiply points manually or via renderables:

```java
Vector2f pos = new Vector2f(32f, 48f);
Vector2f scale = new Vector2f(2f, 2f);
Vector2f origin = new Vector2f(16f, 16f);
Matrix3x2 model = Matrix3x2.fromTransform(pos, rotationRad, scale, origin);
```

## Factory helpers

```java
// Y-down orthographic projection (matches Camera2d)
Matrix3x2 proj = Matrix3x2.ortho(left, right, worldTop, worldBottom);
```

`ortho` maps world rectangle `[left, right] × [top, bottom]` to clip space with **+Y down**.

## OpenGL upload

```java
matrix.upload(uniformLocation);   // writes mat4 uniform
float[] raw = matrix.elements();  // 16 floats, column-major
```

`Transform2f.toMatrix()` produces the same layout used by renderables.

::: tip Reuse matrices
`Matrix3x2` is mutable. In hot loops, keep one scratch matrix and call `set(other)` or chain `identity().translate(...)` instead of allocating each frame.
:::

::: warning Not a general 3×2
Linear algebra texts use 3×2 for 2D affine maps. LLW stores 4×4 for GPU compatibility — do not assume only the top-left 3×2 block is meaningful when hand-editing elements.
:::

## See also

- [Transforms](/math/transforms)
- [Vectors](/math/vectors)
- [Camera](/render/camera)
