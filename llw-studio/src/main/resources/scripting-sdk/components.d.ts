import type { CameraPoint } from "./camera";

import type { Collision2D, Collider2D } from "./physics2d";
import type {
  UIButtonComponent,
  UILabelComponent,
  UITextFieldComponent,
  UIToggleComponent,
} from "./ui";

export type ComponentType =
  | "SpriteRenderer"
  | "Tilemap2D"
  | "Animation2D"
  | "ParticleEmitter"
  | "Camera2D"
  | "AudioSource"
  | "Script"
  | "Rigidbody2D"
  | "BoxCollider2D"
  | "CircleCollider2D"
  | "EdgeCollider2D"
  | "UILabel"
  | "UIButton"
  | "UIToggle"
  | "UITextField";

export type Component =
  | SpriteRendererComponent
  | Tilemap2DComponent
  | Animation2DComponent
  | ParticleEmitterComponent
  | Camera2DComponent
  | AudioSourceComponent
  | ScriptComponentRef
  | Rigidbody2DComponent
  | BoxCollider2DComponent
  | CircleCollider2DComponent
  | EdgeCollider2DComponent
  | UILabelComponent
  | UIButtonComponent
  | UIToggleComponent
  | UITextFieldComponent;

export type { Color } from "./color";

export interface SpriteRendererComponent {
  spriteGuid: string;
  /** @deprecated Use spriteGuid; kept for legacy scenes */
  textureGuid: string;
  color: Color;
  sortingOrder: number;
}

export interface Tilemap2DComponent {
  tilesetTextureGuid: string;
  getTile(layer: number, x: number, y: number): string | null;
  setTile(layer: number, x: number, y: number, spriteGuid: string): void;
  refresh(layer: number, x: number, y: number, radius?: number): void;
}

export interface Animation2DComponent {
  animationGuid: string;
  defaultState: string;
  currentState: string;
  /** @deprecated Use animationGuid + states */
  clipGuid: string;
  playOnStart: boolean;
  speed: number;
  loop: boolean;
  play(): void;
  playState(stateName: string): void;
  stop(): void;
  readonly normalizedTime: number;
}

export interface ParticleEmitterComponent {
  particleSystemGuid: string;
  readonly playing: boolean;
  play(): void;
  stop(): void;
  burst(count: number): void;
}

export interface Camera2DComponent {
  mainCamera: boolean;
  orthographicSize: number;
  depth: number;
  readonly worldX: number;
  readonly worldY: number;
  readonly centerX: number;
  readonly centerY: number;
  readonly worldWidth: number;
  readonly worldHeight: number;
  readonly viewportWidth: number;
  readonly viewportHeight: number;
  readonly aspect: number;
  readonly viewportMouseX: number;
  readonly viewportMouseY: number;
  readonly isActiveMain: boolean;
  worldToScreen(worldX: number, worldY: number): CameraPoint;
  screenToWorld(screenX: number, screenY: number): CameraPoint;
}

export interface AudioSourceComponent {
  clipGuid: string;
  volume: number;
  playOnStart: boolean;
  readonly playing: boolean;
  play(): void;
  stop(): void;
}

/** First script attachment on the entity (editor may attach multiple script components). */
export interface ScriptComponentRef {
  scriptGuid: string;
  enabled: boolean;
}

export interface Rigidbody2DComponent {
  bodyType: string;
  mass: number;
  gravityScale: number;
  velocityX: number;
  velocityY: number;
  freezeRotation: boolean;
  setVelocity(x: number, y: number): void;
  addForce(x: number, y: number): void;
  movePosition(x: number, y: number): void;
}

export interface BoxCollider2DComponent {
  sizeX: number;
  sizeY: number;
  isTrigger: boolean;
}

export interface CircleCollider2DComponent {
  radius: number;
  isTrigger: boolean;
}

export interface EdgeCollider2DComponent {
  isTrigger: boolean;
}

export type { Collision2D, Collider2D };
