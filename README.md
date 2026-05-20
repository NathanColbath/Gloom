# Gloom

Gloom is a Java 17 game/application workspace built around **LLW** — a lightweight LWJGL wrapper for SFML-style 2D rendering, audio, game maths, resources, tooling, and editor/runtime experiments.

The repository currently contains:

- a small root `org.gloom` application entry point;
- the reusable `llw` engine/library module;
- a runtime layer for scenes, scripting, physics, and serialisation;
- an editor-style `llw-studio` application;
- a standalone `llw-player` application and fat-JAR build;
- a VitePress documentation site under `docs/`.

> Status: early project / active development. APIs, package names, and workflows may change as the engine and tools evolve.

---

## Table of contents

- [Project structure](#project-structure)
- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Common Gradle commands](#common-gradle-commands)
- [Modules](#modules)
- [Documentation site](#documentation-site)
- [Development workflow](#development-workflow)
- [Troubleshooting](#troubleshooting)

---

## Project structure

```text
Gloom/
├── build.gradle.kts          # Root Gradle build and root app configuration
├── settings.gradle.kts       # Multi-module Gradle project definition
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── src/                      # Root Gloom sample/application code
├── resources/                # Root project resources placeholder
├── llw/                      # Core Lightweight LWJGL Wrapper library
├── llw-runtime/              # Runtime layer for scenes, scripting, physics, etc.
├── llw-studio/               # Editor/studio application
├── llw-player/               # Standalone player application and packaging tasks
└── docs/                     # VitePress documentation site
```

Gradle includes these modules:

```kotlin
include("llw")
include("llw-runtime")
include("llw-studio")
include("llw-player")
```

---

## Requirements

### Required

- **JDK 17+**
  - The Gradle builds use Java toolchains targeting Java 17.
- **Git**
- **Gradle wrapper** from this repository
  - Prefer `./gradlew` on Linux/macOS and `gradlew.bat` on Windows.

### Optional

- **Node.js + npm**
  - Required only for building or running the VitePress documentation site in `docs/`.
- **A graphics/audio-capable desktop environment**
  - Runtime/editor modules use LWJGL, GLFW, OpenGL/OpenAL, and native libraries.

---

## Quick start

Clone the repository:

```bash
git clone https://github.com/NathanColbath/Gloom.git
cd Gloom
```

Check the Java version:

```bash
java -version
```

Run the Gradle task list:

```bash
./gradlew tasks
```

If your checkout does not preserve executable permissions, run:

```bash
chmod +x gradlew
```

Run tests:

```bash
./gradlew test
```

Run the root Gloom application:

```bash
./gradlew run
```

Run LLW Studio:

```bash
./gradlew :llw-studio:run
```

Run the LLW Player:

```bash
./gradlew :llw-player:run
```

Build the standalone LLW Player fat JAR:

```bash
./gradlew :llw-player:fatJar
```

The root project also exposes an alias:

```bash
./gradlew llw-player-jar
```

---

## Common Gradle commands

| Task | Command |
|------|---------|
| List available tasks | `./gradlew tasks` |
| Build everything | `./gradlew build` |
| Run all tests | `./gradlew test` |
| Run root app | `./gradlew run` |
| Run Studio | `./gradlew :llw-studio:run` |
| Run Player | `./gradlew :llw-player:run` |
| Build Player fat JAR | `./gradlew :llw-player:fatJar` |
| Build docs through Gradle | `./gradlew buildDocs` |
| Generate LLW Javadocs | `./gradlew :llw:javadoc` |

---

## Modules

### Root application

Location: `src/main/java/org/gloom`

The root application is configured with:

```kotlin
application {
    mainClass = "org.gloom.Launcher"
}
```

It depends on the core `:llw` module and acts as the main Gloom application/sample entry point.

### `llw`

Location: `llw/`

The core Lightweight LWJGL Wrapper library. It provides Java 17 APIs around LWJGL for game/application building.

Current areas include:

- rendering/window primitives;
- input and gamepad handling;
- sprites, shapes, text, textures, shaders, and cameras;
- audio playback and OpenAL-backed sound/music helpers;
- maths, geometry, collision, easing, splines, and noise;
- resource loading, asset references, asset packs, and lifecycle helpers;
- logging utilities.

Key dependencies:

- LWJGL `3.4.1`
- JUnit Jupiter `5.10.0` for tests

### `llw-runtime`

Location: `llw-runtime/`

Runtime layer built on top of `:llw`. It adds higher-level systems used by the studio/player flow.

Key dependencies include:

- Jackson Databind / JSR310
- GraalVM Polyglot JavaScript
- JBox2D

### `llw-studio`

Location: `llw-studio/`

An editor/studio application configured with:

```kotlin
application {
    mainClass = "org.llw.studio.StudioLauncher"
}
```

It builds on `:llw-runtime` and `:llw`, and uses ImGui Java/LWJGL bindings for editor UI work.

The studio resource processing step packages the `llw-player` fat JAR into its resources so the editor can use the player runtime.

### `llw-player`

Location: `llw-player/`

Standalone player application configured with:

```kotlin
application {
    mainClass = "org.llw.player.PlayerLauncher"
}
```

Notable tasks:

- `:llw-player:fatJar` — creates `llw-player-all.jar`;
- `:llw-player:jpackageImage` — creates a desktop app image using `jpackage`.

---

## Documentation site

The documentation lives in `docs/` and uses VitePress.

Install dependencies:

```bash
cd docs
npm install
```

Run locally:

```bash
npm run dev
```

Build static docs:

```bash
npm run build
```

Preview the static build:

```bash
npm run preview
```

From the repository root, the docs build can also be triggered with:

```bash
./gradlew buildDocs
```

Documentation areas include:

- tutorials;
- render API guides;
- audio guides;
- maths guides;
- resources and asset packs;
- cookbook examples;
- best practices;
- LLW Studio user and scripting documentation;
- FAQ and SFML migration notes.

---

## Development workflow

A typical local development loop:

```bash
git checkout -b docs/root-comprehensive-readme
./gradlew test
# make a focused change
./gradlew test
git add <changed-files>
git commit -m "docs(root)-add-comprehensive-readme"
git push -u origin HEAD
```

Recommended project conventions:

- keep changes small and focused;
- use atomic commits;
- prefer descriptive branch names such as `docs/root-comprehensive-readme` or `fix/build-make-gradlew-executable`;
- run the narrowest useful verification first, then the broader suite when possible.

---

## Troubleshooting

### `./gradlew: Permission denied`

Your checkout may not have preserved the executable bit on the wrapper script.

```bash
chmod +x gradlew
./gradlew tasks
```

### `JAVA_HOME is not set and no 'java' command could be found in your PATH`

Install JDK 17+ and ensure `java` is on `PATH`, or set `JAVA_HOME` to your JDK installation.

Example check:

```bash
java -version
echo "$JAVA_HOME"
```

### LWJGL native/library issues

The build selects LWJGL natives based on the current OS and architecture. If startup fails with native library errors:

1. confirm your platform is supported by the configured LWJGL native classifier;
2. run from Gradle first so runtime classpaths and native extraction paths are configured;
3. check the generated logs directory if the app/studio creates one.

### Docs build fails with `npm: command not found`

Install Node.js/npm, then run:

```bash
cd docs
npm install
npm run build
```

---

## License

No root licence file is currently present in this repository. Add one before distributing or accepting external contributions.
