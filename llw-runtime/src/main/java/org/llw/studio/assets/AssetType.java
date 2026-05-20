package org.llw.studio.assets;

/**
 * Classification of a studio asset inferred from path extension or directory structure.
 */
public enum AssetType {
    /** Directory entry in the asset browser. */
    FOLDER,
    /** Raster image (PNG, JPEG, WebP). */
    TEXTURE,
    /** Sub-asset slice of a texture spritesheet (virtual, no file on disk). */
    SPRITE,
    /** TrueType or OpenType font. */
    FONT,
    /** Wave or Ogg audio clip. */
    AUDIO,
    /** Serialized scene ({@code .scene.json}). */
    SCENE,
    /** Source script (Java, JavaScript, TypeScript). */
    SCRIPT,
    /** Serialized prefab ({@code .prefab.json}). */
    PREFAB,
    /** Parent animation set ({@code .animation.json}) with states and child clips. */
    ANIMATION,
    /** 2D animation clip ({@code .anim.json}). */
    ANIMATION_CLIP,
    /** Fragment shader graph ({@code .shadergraph.json}). */
    SHADER_GRAPH,
    /** Unrecognized file type. */
    UNKNOWN
}
