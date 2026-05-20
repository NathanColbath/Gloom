import type { AnimationAsset } from "./animation";

export declare namespace Assets {
  function exists(path: string): boolean;
  function resolveGuid(pathOrGuid: string): string;
  function resolvePath(pathOrGuid: string): string;
  function ensureMeta(assetPath: string): string;
  function getAnimation(animationGuid: string): AnimationAsset | null;
}
