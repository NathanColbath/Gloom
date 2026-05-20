export default class PrivateFieldsScript extends Script {
  speed = 5;
  private runtimeOnly = 0;
  protected internal = 1;
  static sharedCounter = 0;
  #hashPrivate = 2;
  target: Entity | null = null;
}
