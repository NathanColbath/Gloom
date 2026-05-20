# Error Handling

LLW favors **fail-fast** exceptions for programmer errors and **graceful degradation** for optional content.

## Common exceptions

| Situation | Typical exception |
|-----------|-------------------|
| Missing classpath asset | `IllegalArgumentException` ("resource not found") |
| Corrupt WAV/OGG | `IllegalArgumentException` / `IllegalStateException` |
| Use after `dispose()` | `IllegalStateException` |
| Invalid GL shader compile | `RuntimeException` with driver log |

Always read the message — paths are included for missing resources.

## Optional assets pattern

The Gloom demo loads fonts defensively:

```java
Font font = loadFont();   // returns null on failure
Text label = null;
if (font != null) {
    label = new Text(font);
    label.setContent("Hello");
}
// skip draw if label == null
```

Use this for non-critical UI. Critical assets should fail loudly during boot.

## OpenAL / OpenGL init

If natives are missing, initialization throws or logs from LWJGL. Check:

1. LWJGL native JARs on the **runtime** classpath
2. `org.lwjgl.system.SharedLibraryExtractPath` when running from an IDE
3. Single OpenGL context on the main thread

See [IDE & Natives](/best-practices/ide-and-natives) and [FAQ](/faq).

## Assertions vs production

Geometry code does not validate negative widths at runtime. Validate user content at load boundaries (level editor export, mod API).

::: tip Centralize loading
A `Assets` class that loads and caches resources gives one place to catch, log, and substitute fallbacks.
:::

::: warning Swallowing audio errors
`Sound.play()` with no buffer silently returns. In debug builds, assert `getBuffer() != null` before play.
:::

## See also

- [FAQ](/faq)
- [Resource Lifecycle](/best-practices/resource-lifecycle)
- [Getting Started](/guide/getting-started)
