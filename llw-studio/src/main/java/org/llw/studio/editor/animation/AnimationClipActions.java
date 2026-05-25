package org.llw.studio.editor.animation;

import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationClipSerializer;
import org.llw.studio.animation.AnimationTrack;
import org.llw.studio.animation.AnimationTrackPaths;
import org.llw.studio.animation.AnimationTrackType;
import org.llw.studio.animation.SpriteKeyframe;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Creates and mutates animation clip assets.
 */
public final class AnimationClipActions {
    private AnimationClipActions() {
    }

    /**
     * @param assets      asset database
     * @param parentFolder folder under Assets, or null for Assets root
     * @param clipName    file base name without extension
     * @return created clip asset GUID, or empty on failure
     */
    public static String createClip(AssetDatabase assets, Path parentFolder, String clipName) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        Path folder = parentFolder == null ? assetsRoot : parentFolder;
        String safeName = clipName == null || clipName.isBlank() ? "NewAnimation" : clipName.trim();
        if (!safeName.endsWith(".anim.json")) {
            safeName = safeName + ".anim.json";
        }
        Path path = uniquePath(folder, safeName);
        String guid = createClipAtPath(assets, path);
        assets.refresh();
        return guid;
    }

    /**
     * @param assets asset database
     * @param path   destination clip path
     * @return new clip GUID (does not refresh the asset index)
     */
    public static String createClipAtPath(AssetDatabase assets, Path path) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        AnimationClip clip = new AnimationClip();
        clip.ensureDefaultTracks();
        AnimationClipSerializer.save(path, clip);
        MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assetsRoot, path);
        meta.type = org.llw.studio.assets.AssetType.ANIMATION_CLIP.name();
        MetaFile.write(assets.projectRoot(), assetsRoot, path, meta);
        return meta.guid;
    }

    /**
     * @param assets          asset database
     * @param parentAnimation parent animation GUID
     * @param clipName        clip base name
     * @return new clip GUID
     */
    public static String createClipUnderAnimation(AssetDatabase assets, String parentAnimation, String clipName)
            throws IOException {
        return AnimationSetActions.addClipToAnimation(assets, parentAnimation, clipName);
    }

    /**
     * Builds sprite keyframes from a texture's slice children in order.
     */
    public static void generateFromSpritesheet(AssetDatabase assets, AnimationClip clip, String textureGuid) {
        if (clip == null || textureGuid == null || textureGuid.isBlank()) {
            return;
        }
        List<StudioAsset> slices = assets.spriteChildren(textureGuid);
        if (slices.isEmpty()) {
            return;
        }
        AnimationTrack track = clip.trackOrCreate(AnimationTrackPaths.SPRITE, AnimationTrackType.SPRITE);
        track.spriteKeyframes.clear();
        float step = clip.frameRate > 0f ? 1f / clip.frameRate : 1f / 12f;
        float time = 0f;
        for (StudioAsset slice : slices) {
            track.spriteKeyframes.add(new SpriteKeyframe(time, slice.guid()));
            time += step; // One key per slice in sheet order at frameRate spacing.
        }
        clip.length = time > 0f ? time - step : 0f;
        track.sortKeyframes();
    }

    private static Path uniquePath(Path folder, String fileName) throws IOException {
        Path candidate = folder.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }
        for (int i = 1; i < 1000; i++) {
            Path next = folder.resolve(base + " (" + i + ")" + ext);
            if (!Files.exists(next)) {
                return next;
            }
        }
        throw new IOException("Could not allocate unique clip path in " + folder);
    }
}
