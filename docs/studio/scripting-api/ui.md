# Scripting API — UI

Screen-space widgets; coordinates are viewport pixels, Y-down.

## UICanvasComponent

| Field | Description |
|-------|-------------|
| `sortingOrder` | Draw order among canvases |
| `enabled` | Master enable |

## UILabelComponent

`text: string`

## UIButtonComponent

| Field | Description |
|-------|-------------|
| `label` | Display string |
| `interactable` | Input enabled |
| `hovered`, `pressed` | Held state |
| `clicked` | True only on click frame |

## UIToggleComponent

`label`, `isOn`, `interactable`

## UITextFieldComponent

| Field / method | Description |
|----------------|-------------|
| `value` | Current text |
| `focused` | Has keyboard focus |
| `setFocus(focus: boolean)` | Focus control |

## Related

- [In-game UI](../in-game-ui.md)
- [Components](components.md)
