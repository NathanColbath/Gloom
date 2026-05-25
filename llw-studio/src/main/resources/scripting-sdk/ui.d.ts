/** UI canvas root (screen-space HUD or world-space overlay). */
export interface UICanvasComponent {
  sortingOrder: number;
  enabled: boolean;
  /** "screenSpace" (default) or "worldSpace" */
  renderMode: string;
  referenceWidth: number;
  referenceHeight: number;
}

/** UI text label (string is writable from scripts). */
export interface UILabelComponent {
  text: string;
}

/** Clickable UI button. */
export interface UIButtonComponent {
  label: string;
  interactable: boolean;
  readonly hovered: boolean;
  readonly pressed: boolean;
  /** True only on the frame the button was clicked. */
  readonly clicked: boolean;
}

/** Checkbox-style UI toggle. */
export interface UIToggleComponent {
  label: string;
  isOn: boolean;
  interactable: boolean;
}

/** Single-line editable UI text field. */
export interface UITextFieldComponent {
  value: string;
  readonly focused: boolean;
  interactable: boolean;
  setFocus(focus: boolean): void;
}
