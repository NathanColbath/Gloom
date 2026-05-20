# Gamepad

`org.llw.render.input.Gamepads` polls up to **four** GLFW joystick slots each frame (`GLFW_JOYSTICK_1` … `GLFW_JOYSTICK_4`).

## Usage

```java
Gamepad pad = input.gamepads().get(0);

if (pad.isConnected()) {
    float moveX = pad.getAxis(GamepadAxis.LEFT_X);
    float moveY = pad.getAxis(GamepadAxis.LEFT_Y);
    if (pad.isButtonDown(GamepadButton.A)) {
        // jump
    }
}
```

`input.gamepads().connectedCount()` returns how many slots report a connected device.

## Mapping

When `glfwJoystickIsGamepad` is true, buttons and axes follow the **GLFW standard gamepad mapping**:

| `GamepadButton` | Typical label |
|-----------------|---------------|
| `A` | A / Cross |
| `B` | B / Circle |
| `X` | X / Square |
| `Y` | Y / Triangle |
| `LEFT_BUMPER` / `RIGHT_BUMPER` | LB / RB |
| `BACK` / `START` | Select / Start |
| `DPAD_*` | D-pad |
| `LEFT_THUMB` / `RIGHT_THUMB` | Stick clicks |

| `GamepadAxis` | Range |
|---------------|-------|
| `LEFT_X`, `LEFT_Y`, `RIGHT_X`, `RIGHT_Y` | [-1, 1] after deadzone |
| `LEFT_TRIGGER`, `RIGHT_TRIGGER` | [0, 1] |

Non-gamepad joysticks fall back to a minimal mapping (first button → `A`, first two axes → left stick).

## Deadzone

Default deadzone is **0.15**. Pass a custom value to `new Gamepads(0.2f)` or adjust via `GamepadMath.applyDeadzone` in tests.

Axes inside the deadzone return `0`; values outside are rescaled to [-1, 1].

## Hot-plug

Controllers are re-detected every frame; no separate connect/disconnect events in v1.

## See also

- [Input](input.md)
