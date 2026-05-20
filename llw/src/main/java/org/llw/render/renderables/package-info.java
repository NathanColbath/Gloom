/**
 * Concrete {@link org.llw.render.graphics.Renderable} implementations for common 2D
 * primitives and textured content.
 *
 * <p>All types in this package extend {@link AbstractTransformable}, which provides position,
 * rotation, scale, and origin in Y-down screen space. Each renderable emits geometry through
 * {@link org.llw.render.gl.OpenGlBackend} when a {@link org.llw.render.graphics.RenderTarget}
 * flushes its draw queue.
 *
 * <p>Typical usage: construct a renderable, configure appearance and transform, then submit it
 * via {@link org.llw.render.graphics.RenderTarget#draw(org.llw.render.graphics.Renderable)}.
 *
 * @see AbstractTransformable
 * @see Sprite
 * @see Rectangle
 * @see Circle
 * @see Text
 * @see VertexGeometry
 */
package org.llw.render.renderables;
