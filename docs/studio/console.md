# Console panel

The Console collects log output from the editor, script runtime, and script bundler (esbuild). Use it to verify gameplay logs and diagnose compile failures.

## Where to find it

Bottom dock, tabbed with **Project** (default). Window title **Console**.

## Log levels

Scripts and engine code may write:

- **Log** — `Logger.log` / `console.log`
- **Warning** — `Logger.warn` / `console.warn`
- **Error** — `Logger.error` / `console.error`

Each line is timestamped and color-coded in the panel.

::: studio-screenshot{file="16-console.png"}
Console showing mixed log levels from play mode.
:::

## Compile errors

When TypeScript fails to bundle (**Assets → Refresh Scripts** or on Play), esbuild errors appear here with file paths and line numbers. Fix the script, refresh, and play again.

::: studio-screenshot{file="17-console-compile-error.png"}
Console showing a script compile error with line number.
:::

## Tips

- Clear is not persistent across sessions; scroll to review older lines during long sessions.
- GraalJS runtime errors during play also route through this sink.

## Related

- [Scripting](scripting.md)
- [Troubleshooting](troubleshooting.md)
