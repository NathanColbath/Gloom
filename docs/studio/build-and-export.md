# Build and export

Studio can package a standalone Windows player from **File → Build Player**. The build deep-scans every scene in the project, includes only referenced assets, compiles TypeScript scripts, writes typed `.pack` files, and stages the `llw-player` runtime.

## Build settings

Open **File → Build Settings…** to configure:

- Product name and version
- Output directory (defaults to `{Project}/Builds/{ProductName}`)
- Window title, size, and vsync for the shipped player

Settings are stored in `.studio/metadata/build-settings.json`.

## Build output

```
{outputDir}/{productName}/
  {productName}.bat           # launcher when jpackage is unavailable
  {productName}.exe           # produced when jpackage is available (Windows)
  runtime/
    llw-player-all.jar
  content/
    game.manifest.json
    textures.pack
    audio.pack
    fonts.pack
    scenes.pack
    scripts.pack
    prefabs.pack
    animations.pack
    metadata.pack
    shaders.pack              # when shader graphs are referenced
```

## Requirements

- Node.js and `npm install` in the project root (for TypeScript bundling during build)
- Build the engine player once: `./gradlew :llw-player:fatJar`
- Windows `.exe` packaging requires a JDK that includes `jpackage`

## Running a build manually

```bat
java -jar runtime/llw-player-all.jar --content content
```

## Related

- [Getting started](getting-started.md)
- [Asset packs](/resources/asset-packs)
