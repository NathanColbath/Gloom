package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: project asset path and GUID resolution for scripts.
 */
public final class AssetsBinding {
    private final AssetDatabase assets;
    private final Path projectRoot;

    /**
     * @param assets      project asset database
     * @param projectRoot project root directory
     */
    public AssetsBinding(AssetDatabase assets, Path projectRoot) {
        this.assets = assets;
        this.projectRoot = projectRoot;
    }

    /**
     * @param path asset path relative to {@code Assets/} or GUID
     * @return {@code true} when the asset exists under the sandbox
     */
    @HostAccess.Export
    public boolean exists(String path) {
        return resolveAbsolutePath(path) != null;
    }

    /**
     * @param pathOrGuid asset path or GUID
     * @return asset GUID, or empty string when not found
     */
    @HostAccess.Export
    public String resolveGuid(String pathOrGuid) {
        if (pathOrGuid == null || pathOrGuid.isBlank()) {
            return "";
        }
        if (assets.get(pathOrGuid) != null) {
            return pathOrGuid;
        }
        Path path = resolveAbsolutePath(pathOrGuid);
        if (path == null) {
            return "";
        }
        StudioAsset asset = assets.getByPath(path);
        return asset == null ? "" : asset.guid();
    }

    /**
     * @param pathOrGuid asset path or GUID
     * @return project-relative path under {@code Assets/}, or empty string
     */
    @HostAccess.Export
    public String resolvePath(String pathOrGuid) {
        if (pathOrGuid == null || pathOrGuid.isBlank()) {
            return "";
        }
        StudioAsset byGuid = assets.get(pathOrGuid);
        if (byGuid != null) {
            String sandboxed = sandboxRelative(byGuid.path());
            return sandboxed == null ? "" : sandboxed;
        }
        Path candidate = sandboxAbsolute(projectRoot.resolve(sanitizeAssetPath(pathOrGuid)));
        if (candidate != null && Files.exists(candidate)) {
            return projectRoot.relativize(candidate).toString().replace('\\', '/');
        }
        return "";
    }

    /**
     * @param animationGuid parent animation asset GUID
     * @return read-only animation metadata, or {@code null}
     */
    @HostAccess.Export
    public AnimationAssetBinding getAnimation(String animationGuid) {
        if (animationGuid == null || animationGuid.isBlank() || assets == null) {
            return null;
        }
        if (assets.animationSet(animationGuid) == null) {
            return null;
        }
        return new AnimationAssetBinding(assets, animationGuid);
    }

    /**
     * @param assetPath path under {@code Assets/}
     * @return meta GUID, or empty string on failure
     */
    @HostAccess.Export
    public String ensureMeta(String assetPath) {
        Path path = resolveAbsolutePath(assetPath);
        if (path == null) {
            return "";
        }
        try {
            Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
            return MetaFile.read(projectRoot, assetsRoot, path).guid;
        } catch (IOException ex) {
            return "";
        }
    }

    private Path resolveAbsolutePath(String pathOrGuid) {
        if (pathOrGuid == null || pathOrGuid.isBlank()) {
            return null;
        }
        StudioAsset byGuid = assets.get(pathOrGuid);
        if (byGuid != null) {
            Path sandboxed = sandboxAbsolute(byGuid.path());
            return sandboxed != null && Files.exists(sandboxed) ? sandboxed : null;
        }
        Path candidate = sandboxAbsolute(projectRoot.resolve(sanitizeAssetPath(pathOrGuid)));
        return candidate != null && Files.exists(candidate) ? candidate : null;
    }

    private static String sanitizeAssetPath(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Asset path must stay within Assets/: " + path);
        }
        if (!normalized.startsWith("Assets/")) {
            normalized = "Assets/" + normalized;
        }
        return normalized;
    }

    private String sandboxRelative(Path path) {
        Path absolute = sandboxAbsolute(path);
        return absolute == null ? null : projectRoot.relativize(absolute).toString().replace('\\', '/');
    }

    private Path sandboxAbsolute(Path path) {
        Path normalized = path.normalize().toAbsolutePath();
        Path assetsRoot = projectRoot.resolve("Assets").normalize().toAbsolutePath();
        if (!normalized.startsWith(assetsRoot)) {
            return null;
        }
        return normalized;
    }
}
