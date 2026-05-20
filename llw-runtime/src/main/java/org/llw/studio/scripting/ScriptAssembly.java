package org.llw.studio.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * Class loader wrapper that resolves legacy Java {@link ScriptBehaviour} types by name.
 */
public final class ScriptAssembly {
    private static final ScriptAssembly EMPTY = new ScriptAssembly(null);

    private final ClassLoader loader;
    private final Map<String, Class<? extends ScriptBehaviour>> behaviours = new HashMap<>();

    /**
     * @param loader class loader containing compiled script classes, or {@code null} for empty
     */
    public ScriptAssembly(ClassLoader loader) {
        this.loader = loader;
        if (loader != null) {
            indexBehaviours();
        }
    }

    /**
     * @return a shared empty assembly with no loader
     */
    public static ScriptAssembly empty() {
        return EMPTY;
    }

    /**
     * @param className fully-qualified behaviour class name
     * @return a new instance, or {@code null} when the type cannot be loaded or constructed
     */
    public ScriptBehaviour create(String className) {
        if (loader == null || className == null || className.isBlank()) {
            return null;
        }
        Class<? extends ScriptBehaviour> type = findClass(className);
        if (type == null) {
            return null;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void indexBehaviours() {
        // Runtime indexing happens on demand when scripts are instantiated.
        behaviours.clear();
    }

    /**
     * @param className fully-qualified class name
     * @return the behaviour type, or {@code null} when not found or not a {@link ScriptBehaviour}
     */
    public Class<? extends ScriptBehaviour> findClass(String className) {
        if (loader == null) {
            return null;
        }
        try {
            Class<?> type = Class.forName(className, true, loader);
            if (ScriptBehaviour.class.isAssignableFrom(type)) {
                return type.asSubclass(ScriptBehaviour.class);
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
