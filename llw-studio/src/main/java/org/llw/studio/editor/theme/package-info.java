/**
 * Modern flat gray ImGui editor theme.
 *
 * <p>Change palette tokens in {@link EditorColors} only; global style is applied once via
 * {@link GloomTheme#apply()} at startup. Scoped overrides use {@link EditorStyle}.
 * Draw-list colors should use {@link ThemeColors#toU32(float[])} with {@link EditorColors} tokens.
 */
package org.llw.studio.editor.theme;
