/**
 * OpenGL-backed rendering implementation: context setup, batched sprites, immediate shapes and
 * text, shader management, and framebuffer objects.
 *
 * <p>{@link OpenGlBackend} is the central entry point used by
 * {@link org.llw.render.graphics.GraphicsContext} and renderables. Higher-level code
 * typically interacts with the graphics API rather than calling these types directly, except when
 * building custom {@link org.llw.render.graphics.Renderable} implementations.
 *
 * <p>Draw ordering for queued renderables is handled by {@link DrawQueue}, which sorts commands
 * by {@link org.llw.render.graphics.DrawState} layer and submission order before
 * invoking each renderable.
 *
 * @see OpenGlBackend
 * @see DrawQueue
 * @see ShaderLibrary
 * @see FramebufferObject
 */
package org.llw.render.gl;
