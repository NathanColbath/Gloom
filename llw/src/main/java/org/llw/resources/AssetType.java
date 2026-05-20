package org.llw.resources;

/**
 * Registered asset kinds managed by {@link ResourceManager}.
 */
public enum AssetType {
    TEXTURE("texture"),
    FONT("font"),
    SOUND("sound"),
    MUSIC("music"),
    RAW("raw");

    private final String wireName;

    AssetType(String wireName) {
        this.wireName = wireName;
    }

    /** Returns the manifest JSON discriminator string. */
    public String wireName() {
        return wireName;
    }

    /**
     * Parses a manifest type string.
     *
     * @param wire manifest value
     * @return matching enum constant
     */
    public static AssetType fromWireName(String wire) {
        for (AssetType type : values()) {
            if (type.wireName.equals(wire)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown asset type: " + wire);
    }
}
