package org.llw.resources.pack;

import org.llw.resources.AssetType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal JSON parser/writer for fixed-schema asset pack manifests (no external deps).
 */
public final class ManifestJson {
    private ManifestJson() {}

    /**
     * Serializes a manifest to JSON.
     *
     * @param entries ordered entry map
     * @return UTF-8 JSON string
     */
    public static String write(Map<String, AssetPackManifest.Entry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"version\":1,\"entries\":{");
        boolean first = true;
        for (Map.Entry<String, AssetPackManifest.Entry> e : entries.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            AssetPackManifest.Entry entry = e.getValue();
            sb.append('"').append(escape(e.getKey())).append("\":{");
            sb.append("\"type\":\"").append(entry.type().wireName()).append("\",");
            sb.append("\"offset\":").append(entry.offset()).append(',');
            sb.append("\"length\":").append(entry.length()).append(',');
            sb.append("\"hint\":\"").append(escape(entry.hint())).append("\"");
            if (entry.type() == AssetType.FONT) {
                sb.append(",\"fontSize\":").append(entry.fontSize());
            }
            sb.append('}');
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Parses a manifest JSON string.
     *
     * @param json manifest text
     * @return parsed manifest
     */
    public static AssetPackManifest parse(String json) {
        int version = readIntField(json, "\"version\":");
        int entriesStart = json.indexOf("\"entries\":{");
        if (entriesStart < 0) {
            throw new IllegalArgumentException("Missing entries object");
        }
        int brace = json.indexOf('{', entriesStart + 10);
        Map<String, AssetPackManifest.Entry> entries = new LinkedHashMap<>();
        int i = brace + 1;
        while (i < json.length()) {
            i = skipWs(json, i);
            if (i < json.length() && json.charAt(i) == '}') {
                break;
            }
            if (json.charAt(i) != '"') {
                throw new IllegalArgumentException("Expected entry key at " + i);
            }
            int keyEnd = json.indexOf('"', i + 1);
            String id = unescape(json.substring(i + 1, keyEnd));
            i = json.indexOf('{', keyEnd);
            int objEnd = findMatchingBrace(json, i);
            String obj = json.substring(i + 1, objEnd);
            AssetType type = AssetType.fromWireName(readStringField(obj, "\"type\":\""));
            int offset = readIntField(obj, "\"offset\":");
            int length = readIntField(obj, "\"length\":");
            String hint = readStringField(obj, "\"hint\":\"");
            int fontSize = obj.contains("\"fontSize\":") ? readIntField(obj, "\"fontSize\":") : 0;
            entries.put(id, new AssetPackManifest.Entry(type, offset, length, hint, fontSize));
            i = objEnd + 1;
            i = skipWs(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++;
            }
        }
        return new AssetPackManifest(version, entries);
    }

    private static int readIntField(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        int start = idx + key.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Integer.parseInt(json.substring(start, end));
    }

    private static String readStringField(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        int start = idx + key.length();
        int end = json.indexOf('"', start);
        return unescape(json.substring(start, end));
    }

    private static int findMatchingBrace(String json, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Unbalanced braces");
    }

    private static int skipWs(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
