package org.llw.studio.build;

/**
 * Typed output pack files produced during a player build.
 */
public enum BuildPackCategory {
    TEXTURES("textures.pack"),
    AUDIO("audio.pack"),
    FONTS("fonts.pack"),
    SCENES("scenes.pack"),
    SCRIPTS("scripts.pack"),
    PREFABS("prefabs.pack"),
    ANIMATIONS("animations.pack"),
    SHADERS("shaders.pack"),
    METADATA("metadata.pack");

    private final String fileName;

    BuildPackCategory(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}
