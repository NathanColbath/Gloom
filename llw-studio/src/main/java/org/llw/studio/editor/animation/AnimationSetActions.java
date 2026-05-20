package org.llw.studio.editor.animation;

import org.llw.studio.assets.AnimationClipEntry;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationSetSerializer;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates parent animation set assets with a default clip and state.
 */
public final class AnimationSetActions {
    private AnimationSetActions() {
    }

    /**
     * @param assets       asset database
     * @param parentFolder folder under Assets, or null for root
     * @param setName      base name without extension
     * @return parent animation GUID
     */
    public static String createAnimation(AssetDatabase assets, Path parentFolder, String setName) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        Path folder = parentFolder == null ? assetsRoot : parentFolder;
        String safeName = setName == null || setName.isBlank() ? "NewAnimation" : setName.trim();
        if (!safeName.endsWith(".animation.json")) {
            safeName = safeName + ".animation.json";
        }
        Path animationPath = uniquePath(folder, safeName);
        AnimationSetDefinition set = new AnimationSetDefinition();
        AnimationSetSerializer.save(animationPath, set);
        MetaFile.MetaData animMeta = MetaFile.read(assets.projectRoot(), assetsRoot, animationPath);
        animMeta.type = AssetType.ANIMATION.name();
        MetaFile.write(assets.projectRoot(), assetsRoot, animationPath, animMeta);

        Path clipPath = uniquePath(animationPath.getParent(), "Idle.anim.json");
        String clipGuid = AnimationClipActions.createClipAtPath(assets, clipPath);
        set.defaultState = "Idle";
        set.states.add(new AnimationStateDefinition("Idle", clipGuid));
        set.clips.add(new AnimationClipEntry(clipGuid, "Idle", clipPath.getFileName().toString()));
        AnimationSetSerializer.save(animationPath, set);
        assets.refresh();
        return animMeta.guid;
    }

    /**
     * Adds a clip file next to a parent animation and registers it on the set.
     */
    /**
     * Creates a state and matching clip with the same name.
     *
     * @return clip GUID for the new state
     */
    public static String createState(AssetDatabase assets, String animationGuid, String stateName) throws IOException {
        String name = stateName == null || stateName.isBlank() ? "NewState" : stateName.trim();
        return addClipToAnimation(assets, animationGuid, name);
    }

    /**
     * Renames a state and updates the animation set registry (call before renaming the clip file).
     */
    public static void renameState(
            AssetDatabase assets,
            String animationGuid,
            String clipGuid,
            String newStateName,
            String newClipFileName
    ) throws IOException {
        StudioAsset parent = requireAnimation(assets, animationGuid);
        AnimationSetDefinition set = requireSet(assets, parent);
        String name = newStateName == null ? "" : newStateName.trim();
        for (int i = 0; i < set.states.size(); i++) {
            AnimationStateDefinition state = set.states.get(i);
            if (clipGuid.equals(state.clipGuid())) {
                if (set.defaultState != null && set.defaultState.equals(state.name())) {
                    set.defaultState = name;
                }
                set.states.set(i, new AnimationStateDefinition(name, clipGuid));
            }
        }
        for (int i = 0; i < set.clips.size(); i++) {
            AnimationClipEntry entry = set.clips.get(i);
            if (clipGuid.equals(entry.guid())) {
                set.clips.set(i, new AnimationClipEntry(clipGuid, name, newClipFileName));
            }
        }
        assets.saveAnimationSet(parent.path(), set);
    }

    /**
     * Sets which state plays by default on the animation asset.
     */
    public static void setDefaultState(AssetDatabase assets, String animationGuid, String stateName) throws IOException {
        StudioAsset parent = requireAnimation(assets, animationGuid);
        AnimationSetDefinition set = requireSet(assets, parent);
        set.defaultState = stateName == null ? "" : stateName.trim();
        assets.saveAnimationSet(parent.path(), set);
    }

    /**
     * Assigns a clip to an existing state name.
     */
    public static void assignStateClip(
            AssetDatabase assets,
            String animationGuid,
            String stateName,
            String clipGuid
    ) throws IOException {
        StudioAsset parent = requireAnimation(assets, animationGuid);
        AnimationSetDefinition set = requireSet(assets, parent);
        boolean found = false;
        for (int i = 0; i < set.states.size(); i++) {
            AnimationStateDefinition state = set.states.get(i);
            if (state.name().equals(stateName)) {
                set.states.set(i, new AnimationStateDefinition(state.name(), clipGuid == null ? "" : clipGuid));
                found = true;
                break;
            }
        }
        if (!found) {
            set.states.add(new AnimationStateDefinition(stateName, clipGuid == null ? "" : clipGuid));
        }
        assets.saveAnimationSet(parent.path(), set);
    }

    public static String addClipToAnimation(AssetDatabase assets, String animationGuid, String clipName) throws IOException {
        StudioAsset parent = assets.get(animationGuid);
        if (parent == null || parent.type() != AssetType.ANIMATION) {
            throw new IOException("Unknown animation asset");
        }
        String safeName = clipName == null || clipName.isBlank() ? "NewClip" : clipName.trim();
        if (!safeName.endsWith(".anim.json")) {
            safeName = safeName + ".anim.json";
        }
        Path clipPath = uniquePath(parent.path().getParent(), safeName);
        String clipGuid = AnimationClipActions.createClipAtPath(assets, clipPath);
        AnimationSetDefinition set = assets.animationSet(animationGuid);
        if (set == null) {
            set = new AnimationSetDefinition();
        }
        String stateName = clipBaseName(safeName);
        set.clips.add(new AnimationClipEntry(clipGuid, stateName, clipPath.getFileName().toString()));
        set.states.add(new AnimationStateDefinition(stateName, clipGuid));
        assets.saveAnimationSet(parent.path(), set);
        return clipGuid;
    }

    private static StudioAsset requireAnimation(AssetDatabase assets, String animationGuid) throws IOException {
        StudioAsset parent = assets.get(animationGuid);
        if (parent == null || parent.type() != AssetType.ANIMATION) {
            throw new IOException("Unknown animation asset");
        }
        return parent;
    }

    private static AnimationSetDefinition requireSet(AssetDatabase assets, StudioAsset parent) throws IOException {
        AnimationSetDefinition set = assets.animationSet(parent.guid());
        if (set == null) {
            throw new IOException("Failed to load animation set");
        }
        return set;
    }

    private static String uniqueStateName(AnimationSetDefinition set, String base) {
        String candidate = base;
        int i = 1;
        while (set.findState(candidate) != null) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }

    private static String clipBaseName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".anim.json")) {
            return fileName.substring(0, fileName.length() - ".anim.json".length());
        }
        return fileName;
    }

    private static Path uniquePath(Path folder, String fileName) throws IOException {
        Files.createDirectories(folder);
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
        throw new IOException("Could not allocate unique path in " + folder);
    }
}
