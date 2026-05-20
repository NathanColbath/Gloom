# Logging (llw)

llw provides a lightweight leveled logging API in `org.llw.util.log`. Logs are written to timestamped files under a configurable directory, with a separate error log for warnings and above.

## Initialization

Call `Log.init` once at process startup (before creating engine subsystems) and `Log.shutdown` in a `finally` block:

```java
Log.init(LogConfig.builder()
        .logDir(Path.of("logs"))
        .minLevel(LogLevel.DEBUG)
        .build());
Log.installUncaughtExceptionHandler();
try {
    // game startup
} finally {
    Log.shutdown();
}
```

### Overrides

| Setting | System property | Environment variable |
|---------|-----------------|----------------------|
| Minimum level | `-Dllw.log.level=DEBUG` | `LLW_LOG_LEVEL` |
| Log directory | `-Dllw.log.dir=logs` | `LLW_LOG_DIR` |

Ship builds typically use `INFO`; use `DEBUG` when diagnosing asset load, GL, or audio issues.

## Log files

| File | Contents |
|------|----------|
| `logs/app.log` | All records at or above the configured `minLevel` |
| `logs/error.log` | `WARN`, `ERROR`, and `FATAL` only |

## Levels

| Level | Use |
|-------|-----|
| `TRACE` | Optional GLFW input events (off by default) |
| `DEBUG` | Asset paths, dimensions, ref counts, decode metadata |
| `INFO` | Subsystem lifecycle (init, dispose) |
| `WARN` | Recoverable misses (missing optional directory, font probe skip) |
| `ERROR` | I/O or decode failure before rethrow |
| `FATAL` | Uncaught thread crash: flush, native popup, `System.exit(1)` |

## Subsystem logger names

Use constants from `Loggers`:

```java
private static final Logger log = Log.get(Loggers.RESOURCES);
```

| Constant | Name |
|----------|------|
| `WINDOW` | `llw.render.window` |
| `GL` | `llw.render.gl` |
| `GRAPHICS` | `llw.render.graphics` |
| `RENDER_RESOURCES` | `llw.render.resources` |
| `AUDIO` | `llw.audio` |
| `AUDIO_RESOURCES` | `llw.audio.resources` |
| `RESOURCES` | `llw.resources` |
| `RESOURCES_PACK` | `llw.resources.pack` |
| `SYSTEM_FONTS` | `llw.render.graphics.system` |
| `MATH` | `llw.math` |

Game code may use a custom name, e.g. `Log.get("Gloom")`.

## DEBUG checklist

When `DEBUG` is enabled, subsystems log enough context to reproduce failures without a debugger:

- **Resources**: asset id, source type (`Classpath`, `File`, `PackSlice`), ref count on acquire/release, lazy-load trigger
- **Textures / fonts**: decoded dimensions, atlas size, pixel height
- **Shaders**: program name; full GL info log on compile/link failure (`ERROR`)
- **OpenGL / OpenAL**: vendor, renderer, version (once at init via `EnvironmentLog`)
- **Audio**: channels, sample rate, duration, AL buffer id
- **Packs**: version, entry count, per-entry offset/length/hint
- **System fonts**: scan directory, indexed family/style/path, probe skips (`WARN`)

Guard expensive DEBUG strings:

```java
if (log.isDebugEnabled()) {
    log.debug("...", expensiveDetail);
}
```

## Frame diagnostics

Per-frame draw and batch logging is **not** emitted every frame. `GraphicsContext.present()` calls `FrameDiagnostics.tick(dt, size)`, which emits one DEBUG line per second (configurable via `frameDiagnosticsIntervalSec`) with FPS, frame ms, draw-queue depth, sprite batch quads flushed, and window size.

Pass frame delta before `present()`:

```java
renderContext.setFrameDelta(dt);
renderContext.present();
```

## ERROR vs FATAL

- **ERROR**: Logged, then typically rethrown as `IllegalStateException` via `LogHelper.logAndThrow`
- **FATAL**: Reserved for unrecoverable top-level failures; shows a native error dialog (tinyfd), flushes logs, and exits

## Tests

Integration tests call `Log.initForTests(LogConfig)` (see `ResourceTestHarness`) so engine code does not write to the project `logs/` directory during CI.
