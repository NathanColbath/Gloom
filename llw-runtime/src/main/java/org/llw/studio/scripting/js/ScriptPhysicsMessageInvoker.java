package org.llw.studio.scripting.js;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.llw.studio.physics.PhysicsContactEvent;
import org.llw.studio.physics.PhysicsMessageType;
import org.llw.studio.scripting.js.bindings.Collision2DBinding;
import org.llw.studio.scripting.js.bindings.Collider2DBinding;
import org.llw.studio.scripting.js.bindings.LoggerBinding;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

/**
 * Unity-style physics message dispatch with method-name and signature overload resolution.
 */
public final class ScriptPhysicsMessageInvoker {
    private ScriptPhysicsMessageInvoker() {
    }

    public static void invoke(ScriptHostApi hostApi, Value instance, String logPrefix, PhysicsContactEvent event) {
        if (instance == null || instance.isNull() || event == null) {
            return;
        }
        String[] names = methodNames(event.type());
        Collision2DBinding collision = new Collision2DBinding(hostApi, event);
        Collider2DBinding collider = new Collider2DBinding(hostApi, event.other());
        for (String name : names) {
            Value callable = instance.getMember(name);
            if (callable == null || callable.isNull() || !callable.canExecute()) {
                continue;
            }
            if (tryInvoke(hostApi, instance, logPrefix, name, event, collision, collider)) {
                return;
            }
        }
    }

    private static boolean tryInvoke(
            ScriptHostApi hostApi,
            Value instance,
            String logPrefix,
            String name,
            PhysicsContactEvent event,
            Collision2DBinding collision,
            Collider2DBinding collider
    ) {
        LoggerBinding.setPrefix(logPrefix);
        try {
            if (event.trigger()) {
                if (invokeWithArg(hostApi, instance, name, hostApi.wrapCollider(collider))) {
                    return true;
                }
                if (invokeWithArg(hostApi, instance, name, hostApi.wrapEntity(
                        hostApi.createEntityBinding(hostApi.scriptContext(), event.other())))) {
                    return true;
                }
                if (invokeWithArg(hostApi, instance, name, hostApi.wrapCollision(collision))) {
                    return true;
                }
            } else {
                if (invokeWithArg(hostApi, instance, name, hostApi.wrapCollision(collision))) {
                    return true;
                }
            }
            return invokeWithArg(hostApi, instance, name);
        } catch (PolyglotException ex) {
            return false;
        } finally {
            LoggerBinding.clearPrefix();
        }
    }

    private static boolean invokeWithArg(ScriptHostApi hostApi, Value instance, String name, Object arg) {
        try {
            instance.invokeMember(name, arg);
            return true;
        } catch (PolyglotException ex) {
            return false;
        }
    }

    private static boolean invokeWithArg(ScriptHostApi hostApi, Value instance, String name) {
        try {
            instance.invokeMember(name);
            return true;
        } catch (PolyglotException ex) {
            return false;
        }
    }

    private static String[] methodNames(PhysicsMessageType type) {
        return switch (type) {
            case COLLISION_ENTER -> new String[] {
                    "onCollisionEnter2D", "OnCollisionEnter2D", "onCollisionEnter", "OnCollisionEnter"
            };
            case COLLISION_STAY -> new String[] {
                    "onCollisionStay2D", "OnCollisionStay2D", "onCollisionStay", "OnCollisionStay"
            };
            case COLLISION_EXIT -> new String[] {
                    "onCollisionExit2D", "OnCollisionExit2D", "onCollisionExit", "OnCollisionExit"
            };
            case TRIGGER_ENTER -> new String[] {
                    "onTriggerEnter2D", "OnTriggerEnter2D", "onTriggerEnter", "OnTriggerEnter"
            };
            case TRIGGER_STAY -> new String[] {
                    "onTriggerStay2D", "OnTriggerStay2D", "onTriggerStay", "OnTriggerStay"
            };
            case TRIGGER_EXIT -> new String[] {
                    "onTriggerExit2D", "OnTriggerExit2D", "onTriggerExit", "OnTriggerExit"
            };
        };
    }
}
