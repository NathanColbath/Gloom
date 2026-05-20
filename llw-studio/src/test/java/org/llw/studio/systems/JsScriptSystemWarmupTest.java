package org.llw.studio.systems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.scripting.js.GraalScriptRuntime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsScriptSystemWarmupTest {
    @TempDir
    Path projectRoot;

    @Test
    void warmupInstancesInvokesStartBeforeFirstUpdate() throws Exception {
        String guid = "warmup-guid";
        Path cacheFile = StudioProjectLayout.scriptCachePath(projectRoot, guid);
        Files.createDirectories(cacheFile.getParent());
        Files.writeString(cacheFile, """
                (function (LLW) {
                  const Script = LLW.Script;
                  return class WarmupPlayer extends Script {
                    start() { globalThis.__warmupStarted = true; }
                    update() {}
                  };
                })
                """);

        Path scriptPath = projectRoot.resolve("Assets/Scripts/WarmupPlayer.js");
        Files.createDirectories(scriptPath.getParent());
        Files.writeString(scriptPath, "export default class WarmupPlayer {}");
        MetaFile.MetaData meta = new MetaFile.MetaData();
        meta.guid = guid;
        meta.type = AssetType.SCRIPT.name();
        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
        MetaFile.write(projectRoot, assetsRoot, scriptPath, meta);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        Scene scene = new Scene();
        scene.createGameObject("Player").addComponent(ScriptComponent.class, org.llw.studio.scripting.ScriptTestSupport.single(guid));

        try (GraalScriptRuntime runtime = new GraalScriptRuntime(null, scene, assets, projectRoot)) {
            runtime.context().eval("js", "globalThis.__warmupStarted = false");
            JsScriptSystem system = new JsScriptSystem(runtime, assets, projectRoot, null);
            system.warmupFactories(Set.of(guid));
            system.warmupInstances(scene);

            assertTrue(runtime.context().eval("js", "globalThis.__warmupStarted").asBoolean());

            system.onUpdate(scene.world(), 0.016f);
            assertTrue(runtime.context().eval("js", "globalThis.__warmupStarted").asBoolean());
        }
    }
}
