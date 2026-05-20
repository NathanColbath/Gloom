export interface AnimationStateInfo {
  readonly name: string;
  readonly clipGuid: string;
}

export interface AnimationAsset {
  readonly guid: string;
  readonly defaultState: string;
  readonly states: readonly string[];
  getState(name: string): AnimationStateInfo | null;
}
