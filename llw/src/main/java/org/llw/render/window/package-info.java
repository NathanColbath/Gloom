/**
 * GLFW-backed window creation, input polling, and event delivery for the Gloom render backend.
 * <p>
 * {@link Window} owns the native GLFW window handle, translates GLFW callbacks into
 * {@link WindowEvent} instances, and exposes keyboard and mouse state queries.
 * {@link WindowSettings} configures initial window properties via a fluent builder.
 * <p>
 * GLFW event polling ({@link Window#pollEvents()}) and buffer swapping
 * ({@link Window#swapBuffers()}) must be invoked on the same thread that created the window,
 * typically the application's main thread.
 */
package org.llw.render.window;
