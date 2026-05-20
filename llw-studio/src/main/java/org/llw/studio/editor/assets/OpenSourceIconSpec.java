package org.llw.studio.editor.assets;

/**
 * Open-source icon set entry resolved through the {@link IconifyIconClient Iconify API}.
 *
 * @param prefix Iconify collection id (e.g. {@code lucide}, MIT-licensed)
 * @param name   icon name within the collection
 * @see <a href="https://iconify.design/docs/api/">Iconify API</a>
 * @see <a href="https://lucide.dev/license">Lucide License (ISC)</a>
 */
public record OpenSourceIconSpec(String prefix, String name) {
    public String iconifyPath() {
        return prefix + "/" + name;
    }
}
