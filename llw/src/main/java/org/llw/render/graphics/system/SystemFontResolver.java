package org.llw.render.graphics.system;

import java.util.List;

/**
 * Platform-specific scanner for installed system fonts.
 */
public interface SystemFontResolver {

    /**
     * @return whether this resolver can scan fonts on the current host
     */
    boolean isSupported();

    /**
     * Enumerates installed font faces.
     *
     * @return discovered faces; empty when unsupported or none found
     */
    List<SystemFontFace> scan();
}
