import type { Entity, Prefab } from "./index";

export declare namespace Scene {
  function findByName(name: string): Entity | null;
  function findAllByName(name: string): Entity[];
  function findByTag(tag: string): Entity | null;
  function findAllByTag(tag: string): Entity[];
  /**
   * Creates an entity in the scene.
   * - No args: empty GameObject
   * - `name`: empty GameObject with that name
   * - `template` Entity: clone that entity (and its children)
   * - `template` Prefab (Inspector prefab field): instantiate that prefab asset
   * - `name` + prefab path/GUID string: instantiate a prefab asset
   * - `null` / unset reference: returns `null` (no spawn)
   */
  function createEntity(name?: string, prefab?: string): Entity;
  function createEntity(template: Entity | Prefab | null): Entity | null;
  function createEntity(name: string, template: Entity | Prefab): Entity;
  function destroyEntity(entity: Entity): void;
}
