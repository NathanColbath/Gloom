export * from "./animation";
export * from "./keys";
export * from "./components";
export * from "./math";
export * from "./color";
export * from "./rect";
export * from "./scene";
export * from "./assets";
export * from "./camera";
export * from "./physics2d";
export * from "./ui";

export { Vector2f, Vec2, Mathf } from "./math";
export { Color } from "./color";
export { Rect2 } from "./rect";
export type { Tilemap2DComponent, Animation2DComponent } from "./components";

import type { Vector2f } from "./math";
import type { Collision2D, Collider2D, Physics2DNamespace } from "./physics2d";
import type {
  UIButtonComponent,
  UICanvasComponent,
  UILabelComponent,
  UITextFieldComponent,
  UIToggleComponent,
} from "./ui";
import type {
  AudioSourceComponent,
  BoxCollider2DComponent,
  Camera2DComponent,
  CircleCollider2DComponent,
  Component,
  ComponentType,
  EdgeCollider2DComponent,
  Rigidbody2DComponent,
  ScriptComponentRef,
  SpriteRendererComponent,
  Tilemap2DComponent,
  Animation2DComponent,
} from "./components";

export type Vector2 = Vector2f;

export declare class Script {
  readonly entity: Entity;
  readonly transform: Transform;
  enabled: boolean;
  /**
   * Public instance fields on your default-export script class are shown in the Inspector and
   * serialized on the entity. Supported types: number, boolean, string, Vector2,
   * {@link Entity} | null (scene entity or prefab reference).
   *
   * Fields are **not** exposed when declared with {@code private}, {@code protected},
   * {@code static}, or ECMAScript private syntax ({@code #name}). Use those for runtime-only
   * state (caches, timers, scratch vectors) that should not be editable per instance in the editor.
   *
   * Inherited members ({@code entity}, {@code transform}, {@code enabled}) are always excluded.
   */
  start?(): void;
  update?(): void;
  fixedUpdate?(): void;
  onEnable?(): void;
  onDisable?(): void;
  onDestroy?(): void;
  onCollisionEnter2D?(collision: Collision2D): void;
  onCollisionEnter?(collision: Collision2D): void;
  onCollisionStay2D?(collision: Collision2D): void;
  onCollisionStay?(collision: Collision2D): void;
  onCollisionExit2D?(collision: Collision2D): void;
  onCollisionExit?(collision: Collision2D): void;
  onTriggerEnter2D?(other: Collider2D): void;
  onTriggerEnter?(other: Collider2D): void;
  onTriggerStay2D?(other: Collider2D): void;
  onTriggerStay?(other: Collider2D): void;
  onTriggerExit2D?(other: Collider2D): void;
  onTriggerExit?(other: Collider2D): void;
}

export type ScriptConstructor<T extends Script = Script> = abstract new (
  ...args: unknown[]
) => T;

export interface Entity {
  readonly id: string;
  name: string;
  tag: string;
  compareTag(tag: string): boolean;
  active: boolean;
  readonly parent: Entity | null;
  readonly children: Entity[];
  readonly sceneId: number;
  readonly transform: Transform;
  readonly worldX: number;
  readonly worldY: number;
  setParent(parent: Entity | null, worldPositionStays?: boolean): void;
  hasComponent(type: ComponentType): boolean;
  getComponent(type: "Camera2D"): Camera2DComponent | null;
  getComponent(type: "SpriteRenderer"): SpriteRendererComponent | null;
  getComponent(type: "Tilemap2D"): Tilemap2DComponent | null;
  getComponent(type: "Animation2D"): Animation2DComponent | null;
  getComponent(type: "AudioSource"): AudioSourceComponent | null;
  getComponent(type: "Script"): ScriptComponentRef | null;
  getComponent(type: "Rigidbody2D"): Rigidbody2DComponent | null;
  getComponent(type: "BoxCollider2D"): BoxCollider2DComponent | null;
  getComponent(type: "CircleCollider2D"): CircleCollider2DComponent | null;
  getComponent(type: "EdgeCollider2D"): EdgeCollider2DComponent | null;
  getComponent(type: "UILabel"): UILabelComponent | null;
  getComponent(type: "UIButton"): UIButtonComponent | null;
  getComponent(type: "UIToggle"): UIToggleComponent | null;
  getComponent(type: "UITextField"): UITextFieldComponent | null;
  getComponent<T extends Script>(scriptClass: ScriptConstructor<T>): T | null;
  getComponent<T extends Component = Component>(type: ComponentType): T | null;
  addComponent(type: ComponentType): void;
  removeComponent(type: ComponentType): void;
  destroy(): void;
}

/**
 * Prefab asset reference assigned in the Inspector on an entity field.
 * Pass to {@link Scene.createEntity} to instantiate the prefab.
 */
export type Prefab = Entity;

export interface Transform {
  position: Vector2;
  /** Local rotation in **degrees** (0 = facing right / +X). Use {@link Math.cosDeg} / {@link Math.sinDeg} or {@link Math.deg2rad} with {@link Math.cos}. */
  rotation: number;
  scale: Vector2;
  readonly worldPosition: Vector2;
  translate(dx: number, dy: number): void;
}

export declare namespace Time {
  const deltaTime: number;
  const time: number;
  const frameCount: number;
}

export declare namespace Input {
  function getKey(key: number): boolean;
  function getKeyDown(key: number): boolean;
  function getKeyUp(key: number): boolean;
  function getMouseButton(button: number): boolean;
  function getMouseButtonDown(button: number): boolean;
  function getMouseButtonUp(button: number): boolean;
  function getMouseX(): number;
  function getMouseY(): number;
  function getScrollX(): number;
  function getScrollY(): number;
  const mouseX: number;
  const mouseY: number;
  const scrollX: number;
  const scrollY: number;
}

export declare namespace Logger {
  function log(message: string): void;
  function warn(message: string): void;
  function error(message: string): void;
}

export declare const Physics2D: Physics2DNamespace;

export declare const console: {
  log(message: string): void;
  warn(message: string): void;
  error(message: string): void;
};
