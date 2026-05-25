package org.llw.render.backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists global renderer choice in {@code ~/.llw-studio/settings.json}.
 */
public final class RendererPreferences {
    private static final Path STORE_PATH = Path.of(
            System.getProperty("user.home", "."),
            ".llw-studio",
            "settings.json"
    );

    private RendererType rendererType = RendererType.OPENGL;

    public RendererPreferences() {
    }

    public RendererPreferences(RendererType rendererType) {
        this.rendererType = rendererType == null ? RendererType.OPENGL : rendererType;
    }

    public static Path storePath() {
        return STORE_PATH;
    }

    public static RendererPreferences load() {
        RendererPreferences prefs = new RendererPreferences();
        if (!Files.exists(STORE_PATH)) {
            return prefs;
        }
        try {
            String json = Files.readString(STORE_PATH, StandardCharsets.UTF_8);
            String id = readStringField(json, "renderer");
            if (id != null) {
                prefs.rendererType = RendererType.fromSettingId(id);
            }
        } catch (IOException ignored) {
        }
        return prefs;
    }

    public RendererType rendererType() {
        return rendererType;
    }

    public void setRendererType(RendererType rendererType) {
        this.rendererType = rendererType == null ? RendererType.OPENGL : rendererType;
    }

    public void save() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            String json = "{\"renderer\":\"" + rendererType.settingId() + "\"}\n";
            Files.writeString(STORE_PATH, json, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static String readStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return null;
        }
        int colon = json.indexOf(':', keyIndex + key.length());
        if (colon < 0) {
            return null;
        }
        int quoteStart = json.indexOf('"', colon + 1);
        if (quoteStart < 0) {
            return null;
        }
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) {
            return null;
        }
        return json.substring(quoteStart + 1, quoteEnd);
    }
}
