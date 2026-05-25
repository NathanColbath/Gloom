package org.llw.studio.materials.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable material asset ({@code .material.json}).
 */
public final class MaterialDocument {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public String shaderSource = MaterialShaderSource.BUILTIN_LIT.name();
    public String shaderGraphGuid = "";
    /** Optional normal map texture GUID override. */
    public String normalMapTextureGuid = "";
    public final List<MaterialProperty> properties = new ArrayList<>();

    public MaterialDocument copy() {
        MaterialDocument copy = new MaterialDocument();
        copy.version = version;
        copy.shaderSource = shaderSource;
        copy.shaderGraphGuid = shaderGraphGuid;
        copy.normalMapTextureGuid = normalMapTextureGuid;
        for (MaterialProperty property : properties) {
            copy.properties.add(property.copy());
        }
        return copy;
    }
}
