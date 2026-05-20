package org.llw.studio.shadergraph.model;

/**
 * Directed link from an output pin to an input pin.
 */
public final class ShaderGraphLink {
    public ShaderGraphPinRef from = new ShaderGraphPinRef();
    public ShaderGraphPinRef to = new ShaderGraphPinRef();

    public ShaderGraphLink copy() {
        ShaderGraphLink copy = new ShaderGraphLink();
        copy.from = from.copy();
        copy.to = to.copy();
        return copy;
    }
}
