# Scripting API — Input and keys

Input is polled each frame from GLFW when the **Game** view is focused.

## Input namespace

```typescript
Input.getKey(key: number): boolean;
Input.getKeyDown(key: number): boolean;
Input.getKeyUp(key: number): boolean;

Input.getMouseButton(button: number): boolean;
Input.getMouseButtonDown(button: number): boolean;
Input.getMouseButtonUp(button: number): boolean;

Input.getMouseX(): number;
Input.getMouseY(): number;
Input.getScrollX(): number;
Input.getScrollY(): number;

// Cached each frame:
Input.mouseX, Input.mouseY, Input.scrollX, Input.scrollY;
```

Mouse position is in **game view** coordinates unless documented otherwise on camera helpers.

## Keys namespace

`Keys` mirrors GLFW key constants, e.g.:

- `Keys.VK_W`, `Keys.VK_A`, `Keys.VK_S`, `Keys.VK_D`
- `Keys.VK_SPACE`, `Keys.VK_ESCAPE`
- `Keys.VK_0` … `Keys.VK_9`

Full list in SDK [`keys.d.ts`](../../../llw-studio/src/main/resources/scripting-sdk/keys.d.ts).

## Example

```typescript
if (core.Input.getKeyDown(core.Keys.VK_SPACE)) {
  core.Logger.log("Jump");
}
```

## Related

- [Game view](../game-view.md)
- [Scripting cookbook](../scripting-cookbook.md)
