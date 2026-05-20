package org.llw.studio.ecs.components;

import org.jbox2d.dynamics.Body;

/**
 * Runtime link between an ECS entity and a Box2D body (play mode only, not serialized).
 */
public final class PhysicsBodyRefComponent {
  /** Box2D body for this entity; {@code null} until {@link org.llw.studio.physics.PhysicsWorld} builds. */
  public Body body;
  /** When {@code true}, transform was edited and should be pushed into the body before the next step. */
  public boolean transformDirty;
  /** When {@code true}, body state should be written back to {@link Transform2DComponent}. */
  public boolean syncTransformFromBody;

  public PhysicsBodyRefComponent() {
  }
}
