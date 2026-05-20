package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: builds JavaScript proxy objects for host bindings.
 */
public final class JsValueFactory {
    private final Context context;

    /**
     * @param context Graal JS context
     */
    public JsValueFactory(Context context) {
        this.context = context;
    }

    /**
     * @param binding host object with {@code getX}/{@code setX} and {@code getY}/{@code setY}
     * @return JavaScript vector proxy
     */
    public Value createVector2Proxy(Object binding) {
        Value javaBinding = Value.asValue(binding);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get x() { return binding.getX(); },
                    set x(value) { binding.setX(value); },
                    get y() { return binding.getY(); },
                    set y(value) { binding.setY(value); },
                  };
                })
                """).execute(javaBinding);
    }

    /**
     * @param binding host object with RGBA accessors
     * @return JavaScript color proxy
     */
    public Value createColorProxy(Object binding) {
        Value javaBinding = Value.asValue(binding);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get r() { return binding.getR(); },
                    set r(value) { binding.setR(value); },
                    get g() { return binding.getG(); },
                    set g(value) { binding.setG(value); },
                    get b() { return binding.getB(); },
                    set b(value) { binding.setB(value); },
                    get a() { return binding.getA(); },
                    set a(value) { binding.setA(value); },
                  };
                })
                """).execute(javaBinding);
    }

    /**
     * @param binding transform host binding
     * @return JavaScript transform proxy
     */
    public Value createTransformProxy(TransformBinding binding) {
        Value javaBinding = Value.asValue(binding);
        return context.eval("js", """
                (function (binding) {
                  const vector = (readX, writeX, readY, writeY) => ({
                    get x() { return readX(); },
                    set x(value) { writeX(value); },
                    get y() { return readY(); },
                    set y(value) { writeY(value); },
                  });
                  return {
                    get position() {
                      return vector(
                        () => binding.position.getX(),
                        (value) => binding.position.setX(value),
                        () => binding.position.getY(),
                        (value) => binding.position.setY(value)
                      );
                    },
                    get rotation() { return binding.getRotation(); },
                    set rotation(value) { binding.setRotation(value); },
                    get scale() {
                      return vector(
                        () => binding.scale.getX(),
                        (value) => binding.scale.setX(value),
                        () => binding.scale.getY(),
                        (value) => binding.scale.setY(value)
                      );
                    },
                    get worldPosition() {
                      return vector(
                        () => binding.getWorldX(),
                        (value) => binding.setWorldX(value),
                        () => binding.getWorldY(),
                        (value) => binding.setWorldY(value)
                      );
                    },
                    translate(dx, dy) { binding.translate(dx, dy); },
                  };
                })
                """).execute(javaBinding);
    }

    /**
     * @param entity    entity host binding
     * @param transform transform proxy value
     * @return host object passed to script constructors
     */
    public Value wrapEntity(EntityBinding entity, Value transform) {
        return context.eval("js", """
                (function (entity, transform) {
                  return { entity: entity, transform: transform };
                })
                """).execute(Value.asValue(entity), transform);
    }
}
