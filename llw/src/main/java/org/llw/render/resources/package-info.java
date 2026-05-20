/**
 * Classpath resource loading utilities for shaders, fonts, and other embedded assets.
 *
 * <p>Paths are resolved relative to the application class loader (for example
 * {@code llw/render/fonts/Roboto-Regular.ttf}). Resources are read once per call; there is no
 * caching layer in this package.
 *
 * @see ResourceLoader
 */
package org.llw.render.resources;
