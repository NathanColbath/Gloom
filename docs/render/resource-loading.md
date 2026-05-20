# Resource Loading

`org.llw.render.resources.ResourceLoader` loads classpath resources as UTF-8 text or raw bytes. It uses the loader that loaded `ResourceLoader` itself (`getClassLoader().getResourceAsStream`).

The class cannot be instantiated — only static helpers.

## Key methods

| Method | Returns | Description |
|--------|---------|-------------|
| `loadText(String classpathPath)` | `String` | Full UTF-8 text content |
| `loadBytes(String classpathPath)` | `byte[]` | Raw byte copy of resource |

## Examples

Texture from packaged PNG:

```java
import org.llw.render.graphics.Texture2d;
import org.llw.render.resources.ResourceLoader;
import org.llw.render.renderables.Sprite;

byte[] pngBytes = ResourceLoader.loadBytes("assets/textures/tile.png");
Texture2d tile = Texture2d.fromBytes(pngBytes);
Sprite ground = new Sprite(tile);
```

Shader sources from classpath (advanced):

```java
import org.llw.render.graphics.ShaderProgram;

String vert = ResourceLoader.loadText("shaders/wave.vert");
String frag = ResourceLoader.loadText("shaders/wave.frag");
ShaderProgram wave = ShaderProgram.fromSources(vert, frag);
```

Font bytes (if loading manually):

```java
import org.llw.render.graphics.Font;

Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 24);
// Font.fromClasspath uses the same classloader pattern internally
```

## Path conventions

Paths are **relative to the classpath root**, not the filesystem:

```
src/main/resources/assets/icon.png  →  "assets/icon.png"
```

No leading slash required; use forward slashes.

## Pitfalls

::: warning
`loadText` assumes UTF-8 encoding. Binary assets (PNG, TTF, compiled meshes) must use `loadBytes`, not `loadText`.
:::

- Missing resources throw `IllegalArgumentException` with `"Resource not found: …"`.
- I/O failures wrap as `IllegalStateException`.
- Resources must be on the runtime classpath of the module/jar that launches the game — IDE working-directory files are not visible unless copied into `resources/`.

::: tip
Keep asset paths as `public static final String` constants in a small `Assets` class to catch typos at compile time and simplify refactors.
:::

## See also

- [Textures](/render/textures)
- [Text & Fonts](/render/text-and-fonts)
- [Shaders](/render/shaders)
- [Resources Overview](/resources/overview) — `ResourceManager` for reference-counted asset lifecycle
