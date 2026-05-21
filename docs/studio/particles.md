# Particle systems

Particle effects are authored as `.particle.json` assets and placed in scenes with the **Particle Emitter** component.

## Assets

- Create via **Assets → Create Particle System** or the Project panel.
- Open a particle asset to edit modules in the **Particles** dock panel.
- The panel provides a live GPU preview, transport controls, and optional **Scene Preview** on the edit scene.

## Modules

| Module | Purpose |
|--------|---------|
| Emission | Rate over time and timed bursts |
| Lifetime | Particle duration (constant, range, or curve) |
| Shape | Spawn point, circle, or box |
| Velocity | Speed and angle distributions |
| Size / color / rotation over lifetime | Curves and gradients |
| Force | Gravity and drag |
| Renderer | Sprite GUID, blend mode, sorting, optional shader graph |
| Texture sheet | Flipbook animation on the sprite texture |
| Sub-emitters | Spawn child systems on birth, death, or collision |
| Noise | Procedural turbulence |
| Trails | Short-lived position history |
| Collision | Physics raycast bounce and lifetime loss |

## Runtime

- Simulation runs on the CPU in `ParticleSimulationSystem` during play mode.
- Rendering uses `ParticleDrawPass` after scene sprites (batched by texture and blend mode).
- Built games pack particle JSON in `particles.pack`; references are collected from scenes and nested sub-emitters.

## Scripting

```javascript
const emitter = entity.getComponent("ParticleEmitter");
emitter.play();
emitter.burst(30);
emitter.stop();
```
