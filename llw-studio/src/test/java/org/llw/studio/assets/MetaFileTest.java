package org.llw.studio.assets;



import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import org.llw.studio.project.StudioProjectLayout;



import java.nio.file.Files;

import java.nio.file.Path;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;



class MetaFileTest {

    @Test

    void upgradesLegacyMetaWithoutType(@TempDir Path dir) throws Exception {

        Path projectRoot = dir;

        Path assetsRoot = dir.resolve("Assets");

        Files.createDirectories(assetsRoot);

        Path texture = assetsRoot.resolve("player.png");

        Files.writeString(texture, "png");

        Path legacyMeta = StudioProjectLayout.legacyAssetsMetaPath(texture);

        Files.writeString(legacyMeta, """

                {

                  "guid" : "1b440e48-10bc-48cd-b4fd-4f5a5dc26f5c"

                }

                """);



        MetaFile.MetaData data = MetaFile.read(projectRoot, assetsRoot, texture);



        assertEquals(AssetType.TEXTURE.name(), data.type);

        Path centralized = MetaFile.metaPath(projectRoot, assetsRoot, texture);

        assertTrue(Files.readString(centralized).contains("\"type\" : \"TEXTURE\""));

        assertFalse(Files.exists(legacyMeta));

    }



    @Test

    void infersPrefabType(@TempDir Path dir) throws Exception {

        Path prefab = dir.resolve("Enemy.prefab.json");

        Files.writeString(prefab, "{\"version\":1,\"objects\":[]}");

        assertEquals(AssetType.PREFAB, MetaFile.inferType(prefab));

    }

}


