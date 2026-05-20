package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ScriptSchemaExtractionTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void extractSchemaSkipsNonInspectorFields(@TempDir Path tempDir) throws Exception {
        assumeTrue(isNodeAvailable(), "Node.js is required to run extract-schema.mjs");

        Path source = tempDir.resolve("PrivateFieldsScript.ts");
        try (InputStream in = getClass().getResourceAsStream("/scripting/PrivateFieldsScript.ts")) {
            assumeTrue(in != null, "Missing test script resource");
            Files.writeString(source, new String(in.readAllBytes()));
        }

        Path extractor = tempDir.resolve("extract-schema.mjs");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("scripting-sdk/extract-schema.mjs")) {
            assumeTrue(in != null, "Missing extract-schema.mjs resource");
            Files.writeString(extractor, new String(in.readAllBytes()));
        }

        Path output = tempDir.resolve("schema.json");
        List<String> command = List.of("node", extractor.toAbsolutePath().toString(),
                source.toAbsolutePath().toString(), output.toAbsolutePath().toString());
        Process process = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .redirectErrorStream(true)
                .start();
        assumeTrue(process.waitFor(30, TimeUnit.SECONDS), "Schema extraction timed out");
        assumeTrue(process.exitValue() == 0, "Schema extraction failed");

        JsonNode root = MAPPER.readTree(output.toFile());
        List<String> names = new ArrayList<>();
        for (JsonNode field : root.path("fields")) {
            names.add(field.path("name").asText());
        }
        assertEquals(List.of("speed", "target"), names);
    }

    @Test
    void extractSchemaRecognizesNewVector2fDefaults(@TempDir Path tempDir) throws Exception {
        assumeTrue(isNodeAvailable(), "Node.js is required to run extract-schema.mjs");

        Path source = tempDir.resolve("Vector2fDefaultsScript.ts");
        try (InputStream in = getClass().getResourceAsStream("/scripting/Vector2fDefaultsScript.ts")) {
            assumeTrue(in != null, "Missing test script resource");
            Files.writeString(source, new String(in.readAllBytes()));
        }

        Path extractor = tempDir.resolve("extract-schema.mjs");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("scripting-sdk/extract-schema.mjs")) {
            assumeTrue(in != null, "Missing extract-schema.mjs resource");
            Files.writeString(extractor, new String(in.readAllBytes()));
        }

        Path output = tempDir.resolve("schema.json");
        List<String> command = List.of("node", extractor.toAbsolutePath().toString(),
                source.toAbsolutePath().toString(), output.toAbsolutePath().toString());
        Process process = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .redirectErrorStream(true)
                .start();
        assumeTrue(process.waitFor(30, TimeUnit.SECONDS), "Schema extraction timed out");
        assumeTrue(process.exitValue() == 0, "Schema extraction failed");

        JsonNode root = MAPPER.readTree(output.toFile());
        JsonNode offset = null;
        JsonNode legacy = null;
        for (JsonNode field : root.path("fields")) {
            if ("offset".equals(field.path("name").asText())) {
                offset = field;
            }
            if ("legacy".equals(field.path("name").asText())) {
                legacy = field;
            }
        }
        assertTrue(offset != null && offset.path("type").asText().equals("vector2"));
        assertEquals(0, offset.path("default").path("x").asDouble(), 0.001);
        assertEquals(1, offset.path("default").path("y").asDouble(), 0.001);
        assertTrue(legacy != null && legacy.path("type").asText().equals("vector2"));
        assertEquals(2, legacy.path("default").path("x").asDouble(), 0.001);
        assertEquals(3, legacy.path("default").path("y").asDouble(), 0.001);
    }

    private static boolean isNodeAvailable() {
        try {
            Process process = new ProcessBuilder("node", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
