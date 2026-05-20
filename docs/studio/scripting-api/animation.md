# Scripting API — Animation

## Animation2DComponent

See [Components](components.md#animation2dcomponent) for `play`, `playState`, `stop`, and state fields.

## Assets namespace (animation metadata)

From `animation.d.ts`:

```typescript
interface AnimationAsset {
  readonly guid: string;
  readonly defaultState: string;
  readonly states: readonly string[];
  getState(name: string): AnimationStateInfo | null;
}

interface AnimationStateInfo {
  readonly name: string;
  readonly clipGuid: string;
}
```

Use `Assets` helpers (see SDK `assets.d.ts`) to query clip duration or state lists at runtime when building custom controllers.

## Related

- [Animation](../animation.md)
- [Components](components.md)
