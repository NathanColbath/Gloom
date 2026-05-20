package org.llw.render.graphics.system;

import org.llw.render.graphics.FontStyle;

import java.nio.file.Path;

/**
 * A single system font face discovered on the host OS.
 *
 * @param family font family name
 * @param style  logical style
 * @param path   filesystem path to the font file
 */
public record SystemFontFace(String family, FontStyle style, Path path) {}
