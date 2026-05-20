# UI theme and widgets

Studio’s editor chrome uses a Unity-inspired dark theme (`UnityTheme`) on top of ImGui. Inspector fields share spacing, labels, and control sizes via reusable widgets under `editor/widgets/`.

## Where to find it

Theme affects all panels. **Preferences** (when exposed in your build) adjust accent color and font scale.

::: studio-screenshot{file="18-preferences-theme.png"}
Preferences with accent color and font scale sliders.
:::

## Inspector field widgets

| Widget | Used for |
|--------|----------|
| `BoolField` | Toggles, active state |
| `FloatField` | Scalars, sliders |
| `Vector2Field` | Position, scale, 2D vectors |
| `ColorField` / `ColorPickerField` | Sprite tint, UI colors |
| `AssetReferenceField` | Texture, audio, generic GUID |
| `SpriteReferenceField` | Sprite Renderer |
| `AnimationReferenceField` | Animation 2D set |
| `ClipReferenceField` | Animation clips |
| `EntityReferenceField` | Script entity links |
| `SearchInput` | Hierarchy filter, Add Component search |

Foldouts use `ComponentFoldout` and `ComponentSection` for per-component headers and remove menus.

::: studio-screenshot{file="05-inspector-transform.png"}
Inspector fields showing consistent spacing and labels.
:::

## Tips

- Long labels truncate with tooltips on hover where implemented.
- Asset reference fields highlight when dragging a compatible asset over them.

## Related

- [Inspector](inspector.md)
