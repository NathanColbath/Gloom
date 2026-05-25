package org.llw.studio.editor.ui;

/**
 * Maps pointer coordinates between a stretched preview image and reference layout pixels.
 */
public final class UiEditorPointer {
    public final float scaleX;
    public final float scaleY;

    private UiEditorPointer(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * @param refWidth   canvas reference width
     * @param refHeight  canvas reference height
     * @param imageWidth displayed image width in the panel
     * @param imageHeight displayed image height in the panel
     */
    public static UiEditorPointer forStretchedImage(int refWidth, int refHeight, float imageWidth, float imageHeight) {
        float rw = Math.max(1, refWidth);
        float rh = Math.max(1, refHeight);
        // Preview image is stretched to panel size; scale maps screen pixels → reference layout pixels.
        return new UiEditorPointer(imageWidth / rw, imageHeight / rh);
    }

    /**
     * @param screenX   ImGui mouse X
     * @param screenY   ImGui mouse Y
     * @param imageMinX top-left X of the preview image
     * @param imageMinY top-left Y of the preview image
     * @return layout-space X
     */
    public float toLayoutX(float screenX, float screenY, float imageMinX, float imageMinY) {
        return (screenX - imageMinX) / Math.max(0.0001f, scaleX);
    }

    /**
     * @return layout-space Y (Y-down, matches {@link org.llw.studio.ui.UiLayout})
     */
    public float toLayoutY(float screenX, float screenY, float imageMinX, float imageMinY) {
        return (screenY - imageMinY) / Math.max(0.0001f, scaleY);
    }
}
