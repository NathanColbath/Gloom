# In-game UI

Screen-space UI is built from **UI Canvas** (root) and child widgets: Label, Button, Toggle, Text Field. Layout uses viewport pixels with **Y-down** coordinates, matching the engine.

## Hierarchy pattern

```
UICanvas (UI Canvas)
├── UILabel
├── UIButton
├── UIToggle
└── UITextField
```

1. Create an empty object; **Add Component → UI Canvas**.
2. Add child objects with widget components.
3. Position children with **Transform 2D** (offsets from canvas; pixels in Screen Space, world units in World Space).

## Render modes (UI Canvas)

| Mode | Use |
|------|-----|
| **Screen Space** | HUD / menus fixed to the viewport. Canvas Transform is a screen-pixel offset. |
| **World Space** | UI tied to a moving object (health bar, nameplate). Follows hierarchy; child offsets are world units. |

Use **View → UI** to lay out widgets in reference resolution without scene camera clutter.

::: studio-screenshot{file="45-ui-hierarchy.png"}
Hierarchy with UICanvas parent and widget children.
:::

::: studio-screenshot{file="46-ui-game-view.png"}
Game view showing buttons and labels during play.
:::

## Widgets

| Component | Role |
|-----------|------|
| **UI Canvas** | Root; `sortingOrder`, enabled flag |
| **UI Label** | Read-only text |
| **UI Button** | Clickable; `hovered`, `pressed`, `clicked` |
| **UI Toggle** | Checkbox; `isOn` |
| **UI Text Field** | Editable text; `focused`, `setFocus` |

## Play-mode input

`UiInputSystem` hit-tests widgets before game-world picking. Buttons fire `clicked` on the frame of release. Text fields capture keyboard focus when clicked.

## Scripts

```typescript
const btn = entity.getComponent("UIButton");
if (btn?.clicked) {
  core.Logger.log("Clicked!");
}
```

See [Scripting API — UI](scripting-api/ui.md).

## Tips

- Only one canvas should drive sorting per screen region unless you manage `sortingOrder`.
- Set `interactable` false to disable input without hiding widgets.

## Related

- [Components reference](components-reference.md)
- [Play mode](play-mode.md)
