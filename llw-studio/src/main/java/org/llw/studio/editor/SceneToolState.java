package org.llw.studio.editor;



/**

 * Mutable scene-view tool selection shared by the toolbar and {@link SceneViewInput}.

 */

public final class SceneToolState {

    private SceneToolMode mode = SceneToolMode.MOVE;



    /** @return current tool mode */

    public SceneToolMode mode() {

        return mode;

    }



    /**

     * @param mode new mode; null falls back to {@link SceneToolMode#HAND}

     */

    public void setMode(SceneToolMode mode) {

        this.mode = mode == null ? SceneToolMode.HAND : mode;

    }

}

