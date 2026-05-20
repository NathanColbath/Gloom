package org.llw.studio.build;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.llw.audio.AudioContext;
import org.llw.render.gl.OpenGlBackend;
import org.llw.resources.ResourceManager;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.project.StudioProjectLayout;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildAssetScannerTest {
    @Test
    @EnabledIf("studioSampleProjectExists")
    void scanFindsReferencedAssetsFromSampleProject() throws Exception {
        Path projectRoot = Paths.get("llw-studio/studio-project").toAbsolutePath().normalize();
        ResourceManager resources = new ResourceManager(new OpenGlBackend(), new AudioContext());
        AssetDatabase assets = new AssetDatabase(projectRoot, resources);
        assets.refresh();

        BuildAssetSet set = BuildAssetScanner.scan(projectRoot, assets);
        assertFalse(set.referencedGuids().isEmpty());
        assertFalse(set.assets(BuildPackCategory.SCENES).isEmpty());
        assertTrue(set.scanLog().stream().anyMatch(line -> line.contains("scene")));
    }

    static boolean studioSampleProjectExists() {
        return Files.isDirectory(Paths.get("llw-studio/studio-project/Assets"))
                && Files.isRegularFile(Paths.get("llw-studio/studio-project/Scenes/Main.scene.json"));
    }
}
