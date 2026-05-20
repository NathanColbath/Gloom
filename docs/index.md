---
layout: home

hero:
  image:
    src: /llw-studio-logo.png
    alt: LLW Studio — LWJGL Lightweight Wrapper
  name: LLW
  text: Lightweight LWJGL Wrapper
  tagline: SFML-style 2D rendering, spatial audio, and game math for Java 17+
  actions:
    - theme: brand
      text: Start Tutorial
      link: /tutorials/01-window
    - theme: alt
      text: Cookbook
      link: /cookbook/mouse-picking

features:
  - title: LLW Studio
    details: Unity-style 2D editor — scenes, prefabs, tilemaps, TypeScript scripting, physics, and play mode on top of the llw engine.
    link: /studio/
  - title: org.llw.render
    details: Window, OpenGL 2D drawing, sprites, shapes, text, camera, offscreen targets — Y-down screen space. 20+ reference pages and 10-step tutorial track.
    link: /render/overview
  - title: org.llw.audio
    details: OpenAL sound effects and streaming music with a simple SFML-inspired API. Sounds, music, listener, and format guides.
    link: /audio/overview
  - title: org.llw.math
    details: 2D vectors, transforms, geometry, collision, easing, splines, and Perlin noise — shared with the render stack.
    link: /math/overview
---

## Documentation map

| Section | What's inside |
|---------|----------------|
| [Studio](/studio/) | LLW Studio editor — panels, assets, scripting, tilemaps, physics, play mode |
| [Tutorials](/tutorials/01-window) | 10-chapter SFML-style learning path from first window to game loop |
| [Render](/render/overview) | Per-type API reference — window, sprites, camera, shaders, … |
| [Cookbook](/cookbook/mouse-picking) | 17 task recipes with nested variations |
| [Best Practices](/best-practices/resource-lifecycle) | Lifecycle, performance, coordinates, IDE setup |
| [FAQ](/faq) | Troubleshooting black screens, audio, natives, zoom mouse |
| [SFML Migration](/sfml-migration) | C++ SFML → Java LLW type mapping |

## Architecture

```mermaid
flowchart LR
    App[Your Game] --> Render[org.llw.render]
    App --> Audio[org.llw.audio]
    App --> Math[org.llw.math]
    Render --> LWJGL[LWJGL GLFW + OpenGL]
    Audio --> OpenAL[LWJGL OpenAL + STB]
    Math --> Pure[Pure Java]
```

## Quick start

```java
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;
import org.llw.render.core.Color;

Window window = new Window(new WindowSettings().title("Hello LLW").size(800, 600));
GraphicsContext gfx = new GraphicsContext(window);

while (gfx.isActive()) {
    gfx.pollEvents();
    gfx.clear(new Color(30, 30, 40));
    gfx.present();
}
gfx.dispose();
```

Continue with [Tutorial 1 — Your First Window](/tutorials/01-window) or [Getting Started](/guide/getting-started).
