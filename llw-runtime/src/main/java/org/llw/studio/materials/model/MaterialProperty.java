package org.llw.studio.materials.model;

/**
 * Named shader property with a default value stored on the material asset.
 */
public final class MaterialProperty {
    public String name = "";
    public String type = MaterialPropertyType.FLOAT.name();
    public float floatValue;
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;
    public String textureGuid = "";

    public MaterialProperty copy() {
        MaterialProperty copy = new MaterialProperty();
        copy.name = name;
        copy.type = type;
        copy.floatValue = floatValue;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.textureGuid = textureGuid;
        return copy;
    }
}
