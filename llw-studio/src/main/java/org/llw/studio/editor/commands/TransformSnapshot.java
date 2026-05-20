package org.llw.studio.editor.commands;



import org.llw.studio.ecs.components.Transform2DComponent;



/**

 * Immutable copy of {@link Transform2DComponent} fields for undo and gizmo drag.

 *

 * @param x        local X

 * @param y        local Y

 * @param rotation local rotation in degrees

 * @param scaleX   local scale X

 * @param scaleY   local scale Y

 */

public record TransformSnapshot(float x, float y, float rotation, float scaleX, float scaleY) {

    /**

     * @param transform live component to copy

     * @return snapshot of current values

     */

    public static TransformSnapshot from(Transform2DComponent transform) {

        return new TransformSnapshot(transform.x, transform.y, transform.rotation, transform.scaleX, transform.scaleY);

    }



    /**

     * Writes this snapshot into a live component.

     *

     * @param transform component to update

     */

    public void applyTo(Transform2DComponent transform) {

        transform.x = x;

        transform.y = y;

        transform.rotation = rotation;

        transform.scaleX = scaleX;

        transform.scaleY = scaleY;

    }



    /**

     * @param other   snapshot to compare

     * @param epsilon maximum per-field delta treated as equal

     * @return true if all fields are within {@code epsilon}

     */

    public boolean equalsApprox(TransformSnapshot other, float epsilon) {

        return Math.abs(x - other.x) <= epsilon

                && Math.abs(y - other.y) <= epsilon

                && Math.abs(rotation - other.rotation) <= epsilon

                && Math.abs(scaleX - other.scaleX) <= epsilon

                && Math.abs(scaleY - other.scaleY) <= epsilon;

    }

}

