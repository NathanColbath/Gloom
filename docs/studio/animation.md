# Animation

Studio supports 2D sprite animation through **animation sets** (state machines), **clips**, and the **Animation 2D** component.

## Asset types

| Asset | File | Purpose |
|-------|------|---------|
| Animation set | `*.animation.json` | Named states → clip GUIDs |
| Animation clip | `*.anim.json` | Frame keys, timing, sprite references |

Create assets via **Assets → Create Animation** and **Create Animation Clip**.

## Animation panel

Docked bottom-center by default (**Animation** window). Use it to:

- Select an animation set asset
- Add or rename states
- Assign clips to states
- Preview state names

::: studio-screenshot{file="35-animation-panel.png"}
Animation panel with states list and clip assignment.
:::

::: studio-screenshot{file="36-create-animation-state.png"}
Create State dialog for a new animation state name.
:::

## Animation 2D component

On an entity with **Sprite Renderer**:

1. **Add Component → Animation 2D**
2. Assign **animation** set GUID
3. Set **default state** and options: play on start, speed, loop

At play, `AnimationSystem` samples the active clip and updates the sprite frame.

::: studio-screenshot{file="37-inspector-animation2d.png"}
Animation 2D component fields in Inspector.
:::

## From scripts

```typescript
const anim = entity.getComponent("Animation2D");
anim?.playState("Run");
anim?.stop();
```

See [Scripting API — Animation](scripting-api/animation.md).

## Related

- [Project and assets](project-and-assets.md)
- [Systems reference](systems-reference.md)
