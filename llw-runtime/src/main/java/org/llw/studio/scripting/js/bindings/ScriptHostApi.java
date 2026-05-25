package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;
import org.llw.studio.log.StudioLogSink;

import java.nio.file.Path;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: installs Graal host bindings and builds script hosts.
 */
public final class ScriptHostApi {
    private final Context context;
    private final JsValueFactory valueFactory;
    private final ScriptContext scriptContext;
    private final SceneBinding scene;
    private final AssetsBinding assets;
    private final MathBinding.Vec2Factory vec2 = new MathBinding.Vec2Factory();
    private final MathBinding.Mathf mathf = new MathBinding.Mathf();
    private final ColorMathBinding.ColorFactory colorFactory = new ColorMathBinding.ColorFactory();
    private final Rect2Binding.Rect2Factory rect2Factory = new Rect2Binding.Rect2Factory();
    private final TimeBinding time = new TimeBinding();
    private final InputBinding input = new InputBinding();
    private final CameraBinding camera;
    private final LoggerBinding logger;
    private final Value scriptBaseClass;

    /**
     * @param context        Graal JS context
     * @param console        editor console for logs
     * @param playScene      cloned play-mode scene
     * @param assetDatabase  project assets
     * @param projectRoot    project root directory
     */
    public ScriptHostApi(
            Context context,
            StudioLogSink console,
            Scene playScene,
            AssetDatabase assetDatabase,
            Path projectRoot
    ) {
        this.context = context;
        this.valueFactory = new JsValueFactory(context);
        this.scriptContext = new ScriptContext(playScene, assetDatabase, projectRoot);
        this.scene = new SceneBinding(scriptContext, this);
        this.assets = assetDatabase == null ? null : new AssetsBinding(assetDatabase, projectRoot);
        this.logger = new LoggerBinding(console);
        this.camera = new CameraBinding(scriptContext, this);
        this.scriptBaseClass = context.eval("js", """
                class Script {
                  constructor(host) {
                    this.entity = host.entity;
                    this.transform = host.transform;
                    this.enabled = host.enabled;
                  }
                }
                Script
                """);
    }

    /** Registers global {@code LLW}, {@code Scene}, {@code Time}, and related namespaces. */
    /**
     * @param x X component
     * @param y Y component
     * @return JavaScript {@code Vec2} proxy with {@code mul}, {@code add}, and related helpers
     */
    public Value createVec2(double x, double y) {
        Value vec2 = context.getBindings("js").getMember("Vec2");
        return vec2.invokeMember("create", x, y);
    }

    public void install() {
        context.getBindings("js").putMember("LLW", createLlwNamespace());
        context.getBindings("js").putMember("Scene", scene);
        if (assets != null) {
            context.getBindings("js").putMember("Assets", assets);
        }
        SdkGlobalsInstaller.install(context, vec2, mathf, colorFactory, rect2Factory);
        context.getBindings("js").putMember("Math", wrapMathNamespace());
        context.getBindings("js").putMember("Time", wrapTimeNamespace());
        context.getBindings("js").putMember("Input", wrapInputNamespace());
        context.getBindings("js").putMember("Camera", wrapCameraNamespace());
        context.getBindings("js").putMember("Logger", logger);
        context.getBindings("js").putMember("console", createConsoleProxy());
        context.getBindings("js").putMember("Physics2D", Value.asValue(new Physics2DBinding()));
    }

    public Value wrapCollision(Collision2DBinding binding) {
        if (binding == null) {
            return context.asValue(null);
        }
        return createCollisionProxy(binding);
    }

    /**
     * @param binding collision message payload
     * @return JavaScript collision proxy with {@code other} and velocity fields
     */
    public Value createCollisionProxy(Collision2DBinding binding) {
        return context.eval("js", """
                (function (binding) {
                  return {
                    get other() { return binding.getOther(); },
                    get relativeVelocityX() { return binding.getRelativeVelocityX(); },
                    get relativeVelocityY() { return binding.getRelativeVelocityY(); },
                    getRelativeVelocityX() { return binding.getRelativeVelocityX(); },
                    getRelativeVelocityY() { return binding.getRelativeVelocityY(); },
                  };
                })
                """).execute(Value.asValue(binding));
    }

    public Value wrapCollider(Collider2DBinding binding) {
        return Value.asValue(binding);
    }

    private Value createLlwNamespace() {
        return context.eval("js", "(function (Script) { return { Script: Script }; })").execute(scriptBaseClass);
    }

    private Value createConsoleProxy() {
        return context.eval("js", """
                (function (logger) {
                  return {
                    log(message) { logger.log(String(message)); },
                    warn(message) { logger.warn(String(message)); },
                    error(message) { logger.error(String(message)); },
                  };
                })
                """).execute(Value.asValue(logger));
    }

    private Value wrapInputNamespace() {
        return context.eval("js", """
                (function (input) {
                  return {
                    getKey: (key) => input.getKey(key),
                    getKeyDown: (key) => input.getKeyDown(key),
                    getKeyUp: (key) => input.getKeyUp(key),
                    getMouseButton: (button) => input.getMouseButton(button),
                    getMouseButtonDown: (button) => input.getMouseButtonDown(button),
                    getMouseButtonUp: (button) => input.getMouseButtonUp(button),
                    getMouseX: () => input.getMouseX(),
                    getMouseY: () => input.getMouseY(),
                    getScrollX: () => input.getScrollX(),
                    getScrollY: () => input.getScrollY(),
                    get mouseX() { return input.getMouseX(); },
                    get mouseY() { return input.getMouseY(); },
                    get scrollX() { return input.getScrollX(); },
                    get scrollY() { return input.getScrollY(); },
                  };
                })
                """).execute(Value.asValue(input));
    }

    private Value wrapTimeNamespace() {
        return context.eval("js", """
                (function (time) {
                  return {
                    get deltaTime() { return time.getDeltaTime(); },
                    get time() { return time.getTime(); },
                    get frameCount() { return time.getFrameCount(); },
                  };
                })
                """).execute(Value.asValue(time));
    }

    private Value wrapCameraNamespace() {
        return context.eval("js", """
                (function (camera) {
                  return {
                    get active() { return camera.isActive(); },
                    get main() { return camera.getMain(); },
                  };
                })
                """).execute(Value.asValue(camera));
    }

    private Value wrapMathNamespace() {
        return context.eval("js", """
                (function (math) {
                  return {
                    clamp: (value, min, max) => math.clamp(value, min, max),
                    lerp: (a, b, t) => math.lerp(a, b, t),
                    inverseLerp: (a, b, value) => math.inverseLerp(a, b, value),
                    smoothstep: (t) => math.smoothstep(t),
                    min: (a, b) => math.min(a, b),
                    max: (a, b) => math.max(a, b),
                    abs: (value) => math.abs(value),
                    round: (value) => math.round(value),
                    floor: (value) => math.floor(value),
                    ceil: (value) => math.ceil(value),
                    sin: (value) => math.sin(value),
                    cos: (value) => math.cos(value),
                    cosDeg: (degrees) => math.cosDeg(degrees),
                    sinDeg: (degrees) => math.sinDeg(degrees),
                    atan2: (y, x) => math.atan2(y, x),
                    sqrt: (value) => math.sqrt(value),
                    deg2rad: (degrees) => math.deg2rad(degrees),
                    rad2deg: (radians) => math.rad2deg(radians),
                    random: () => math.random(),
                    randomRange: (min, max) => math.randomRange(min, max),
                  };
                })
                """).execute(Value.asValue(mathf));
    }

    /**
     * @return shared play-mode script context
     */
    public ScriptContext scriptContext() {
        return scriptContext;
    }

    /**
     * @param context play-mode script context
     * @param entity  target entity
     * @return host entity binding (not yet wrapped for JS)
     */
    public EntityBinding createEntityBinding(ScriptContext context, EntityId entity) {
        EntityBinding binding = new EntityBinding(context, this, entity);
        return binding;
    }

    /**
     * @param binding entity host binding
     * @return JavaScript entity proxy, or JS null
     */
    public Value wrapEntity(EntityBinding binding) {
        if (binding == null) {
            return context.asValue(null);
        }
        return createEntityProxy(binding);
    }

    /**
     * @param prefabGuid prefab asset GUID
     * @return JavaScript prefab template value, or JS null
     */
    public Value wrapPrefabTemplate(String prefabGuid) {
        if (prefabGuid == null || prefabGuid.isBlank()) {
            return context.asValue(null);
        }
        return createPrefabTemplateProxy(new PrefabTemplateBinding(prefabGuid));
    }

    /**
     * @param binding prefab asset reference
     * @return JavaScript prefab template proxy with {@code prefabGuid}, or JS null
     */
    public Value createPrefabTemplateProxy(PrefabTemplateBinding binding) {
        if (binding == null || binding.prefabGuid().isBlank()) {
            return context.asValue(null);
        }
        Value javaBinding = Value.asValue(binding);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get prefabGuid() { return binding.getPrefabGuid(); },
                  };
                })
                """).execute(javaBinding);
    }

    /**
     * @param binding entity host binding
     * @return JavaScript entity proxy with hierarchy and component accessors
     */
    public Value createEntityProxy(EntityBinding binding) {
        Value javaBinding = Value.asValue(binding);
        Value host = Value.asValue(this);
        return context.eval("js", """
                (function (binding, host) {
                  const wrap = (entity) => host.wrapEntity(entity);
                  const transform = host.createTransformProxy(binding);
                  const resolveComponent = (type, required) => {
                    if (type == null) {
                      return null;
                    }
                    if (typeof type === "function") {
                      const name = type.name;
                      if (!name) {
                        return null;
                      }
                      return required
                        ? binding.requireScriptComponent(name)
                        : binding.getScriptComponent(name);
                    }
                    return required
                      ? binding.requireComponent(String(type))
                      : binding.getComponent(String(type));
                  };
                  return {
                    get id() { return binding.getId(); },
                    get name() { return binding.getName(); },
                    set name(value) { binding.setName(value); },
                    get tag() { return binding.getTag(); },
                    set tag(value) { binding.setTag(value); },
                    compareTag(value) { return binding.compareTag(value); },
                    get active() { return binding.isActive(); },
                    set active(value) { binding.setActive(value); },
                    get parent() { return wrap(binding.getParent()); },
                    get children() { return binding.getChildren().map(wrap); },
                    get sceneId() { return binding.getSceneId(); },
                    get transform() { return transform; },
                    get worldX() { return binding.getWorldX(); },
                    get worldY() { return binding.getWorldY(); },
                    setParent(parent, worldPositionStays) {
                      binding.setParent(parent, worldPositionStays !== false);
                    },
                    hasComponent(type) { return binding.hasComponent(String(type)); },
                    getComponent(type) { return resolveComponent(type, false); },
                    requireComponent(type) { return resolveComponent(type, true); },
                    addComponent(type) { binding.addComponent(String(type)); },
                    removeComponent(type) { binding.removeComponent(String(type)); },
                    destroy() { binding.destroy(); },
                  };
                })
                """).execute(javaBinding, host);
    }

    /**
     * @param binding entity whose transform is exposed
     * @return JavaScript transform proxy, or JS null
     */
    @HostAccess.Export
    public Value createTransformProxy(EntityBinding binding) {
        if (binding == null) {
            return context.asValue(null);
        }
        return valueFactory.createTransformProxy(new TransformBinding(scriptContext.world(), binding.entityId()));
    }

    /**
     * @param context        play-mode script context
     * @param entity         script owner entity
     * @param scriptEnabled  initial {@code enabled} flag for the script instance
     * @return host object passed to script class constructors
     */
    public Value createHost(ScriptContext context, EntityId entity, boolean scriptEnabled) {
        EntityBinding entityBinding = createEntityBinding(context, entity);
        Value entityProxy = createEntityProxy(entityBinding);
        Value transformBinding = valueFactory.createTransformProxy(new TransformBinding(context.world(), entity));
        return this.context.eval("js", """
                (function (entity, transform, enabled) {
                  return { entity: entity, transform: transform, enabled: enabled };
                })
                """).execute(entityProxy, transformBinding, scriptEnabled);
    }

    /**
     * @param type    component type name
     * @param binding host binding from {@link ComponentBindings}
     * @return JavaScript component proxy, or the raw host value
     */
    public Value wrapComponent(String type, Object binding) {
        if (binding == null) {
            return context.asValue(null);
        }
        if ("SpriteRenderer".equals(type) && binding instanceof SpriteRendererBinding sprite) {
            return createSpriteRendererProxy(sprite);
        }
        if ("Camera2D".equals(type) && binding instanceof Camera2DBinding camera2d) {
            return createCamera2DProxy(camera2d);
        }
        if ("Animation2D".equals(type) && binding instanceof Animation2DBinding animation2d) {
            return createAnimation2DProxy(animation2d);
        }
        if ("Tilemap2D".equals(type) && binding instanceof Tilemap2DBinding tilemap) {
            return createTilemap2DProxy(tilemap);
        }
        if ("UILabel".equals(type) && binding instanceof UILabelBinding label) {
            return createUILabelProxy(label);
        }
        if ("UIButton".equals(type) && binding instanceof UIButtonBinding button) {
            return createUIButtonProxy(button);
        }
        if ("UIToggle".equals(type) && binding instanceof UIToggleBinding toggle) {
            return createUIToggleProxy(toggle);
        }
        if ("UITextField".equals(type) && binding instanceof UITextFieldBinding field) {
            return createUITextFieldProxy(field);
        }
        if ("Rigidbody2D".equals(type) && binding instanceof Rigidbody2DBinding rigidbody) {
            return createRigidbody2DProxy(rigidbody);
        }
        if ("ParticleEmitter".equals(type) && binding instanceof ParticleEmitterBinding emitter) {
            return createParticleEmitterProxy(emitter);
        }
        if ("AudioSource".equals(type) && binding instanceof AudioSourceBinding audio) {
            return createAudioSourceProxy(audio);
        }
        return Value.asValue(binding);
    }

    private Value createParticleEmitterProxy(ParticleEmitterBinding emitter) {
        Value javaBinding = Value.asValue(emitter);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get particleSystemGuid() { return binding.getParticleSystemGuid(); },
                    set particleSystemGuid(value) { binding.setParticleSystemGuid(value); },
                    get playing() { return binding.isPlaying(); },
                    play: () => binding.play(),
                    stop: () => binding.stop(),
                    burst: (count) => binding.burst(count),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createAudioSourceProxy(AudioSourceBinding audio) {
        Value javaBinding = Value.asValue(audio);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get clipGuid() { return binding.getClipGuid(); },
                    set clipGuid(value) { binding.setClipGuid(value); },
                    get volume() { return binding.getVolume(); },
                    set volume(value) { binding.setVolume(value); },
                    get playOnStart() { return binding.getPlayOnStart(); },
                    set playOnStart(value) { binding.setPlayOnStart(value); },
                    get playing() { return binding.getPlaying(); },
                    play: () => binding.play(),
                    stop: () => binding.stop(),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createRigidbody2DProxy(Rigidbody2DBinding rigidbody) {
        Value javaBinding = Value.asValue(rigidbody);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get bodyType() { return binding.getBodyType(); },
                    set bodyType(value) { binding.setBodyType(value); },
                    get mass() { return binding.getMass(); },
                    set mass(value) { binding.setMass(value); },
                    get gravityScale() { return binding.getGravityScale(); },
                    set gravityScale(value) { binding.setGravityScale(value); },
                    get velocityX() { return binding.getVelocityX(); },
                    get velocityY() { return binding.getVelocityY(); },
                    get freezeRotation() { return binding.getFreezeRotation(); },
                    set freezeRotation(value) { binding.setFreezeRotation(value); },
                    setVelocity(x, y) { binding.setVelocity(x, y); },
                    addForce(x, y) { binding.addForce(x, y); },
                    movePosition(x, y) { binding.movePosition(x, y); },
                  };
                })
                """).execute(javaBinding);
    }

    private Value createCamera2DProxy(Camera2DBinding camera2d) {
        Value javaBinding = Value.asValue(camera2d);
        return context.eval("js", """
                (function (binding) {
                  const wrapPoint = (value) => ({
                    get x() { return value.getX(); },
                    get y() { return value.getY(); },
                  });
                  return {
                    get mainCamera() { return binding.getMainCamera(); },
                    set mainCamera(value) { binding.setMainCamera(value); },
                    get orthographicSize() { return binding.getOrthographicSize(); },
                    set orthographicSize(value) { binding.setOrthographicSize(value); },
                    get depth() { return binding.getDepth(); },
                    set depth(value) { binding.setDepth(value); },
                    get worldX() { return binding.getWorldX(); },
                    get worldY() { return binding.getWorldY(); },
                    get centerX() { return binding.getCenterX(); },
                    get centerY() { return binding.getCenterY(); },
                    get worldWidth() { return binding.getWorldWidth(); },
                    get worldHeight() { return binding.getWorldHeight(); },
                    get viewportWidth() { return binding.getViewportWidth(); },
                    get viewportHeight() { return binding.getViewportHeight(); },
                    get aspect() { return binding.getAspect(); },
                    get viewportMouseX() { return binding.getViewportMouseX(); },
                    get viewportMouseY() { return binding.getViewportMouseY(); },
                    get isActiveMain() { return binding.isActiveMain(); },
                    worldToScreen: (x, y) => wrapPoint(binding.worldToScreen(x, y)),
                    screenToWorld: (x, y) => wrapPoint(binding.screenToWorld(x, y)),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createAnimation2DProxy(Animation2DBinding animation2d) {
        Value javaBinding = Value.asValue(animation2d);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get animationGuid() { return binding.getAnimationGuid(); },
                    set animationGuid(value) { binding.setAnimationGuid(value); },
                    get defaultState() { return binding.getDefaultState(); },
                    set defaultState(value) { binding.setDefaultState(value); },
                    get currentState() { return binding.getCurrentState(); },
                    set currentState(value) { binding.setCurrentState(value); },
                    get clipGuid() { return binding.getClipGuid(); },
                    set clipGuid(value) { binding.setClipGuid(value); },
                    get playOnStart() { return binding.getPlayOnStart(); },
                    set playOnStart(value) { binding.setPlayOnStart(value); },
                    get speed() { return binding.getSpeed(); },
                    set speed(value) { binding.setSpeed(value); },
                    get loop() { return binding.getLoop(); },
                    set loop(value) { binding.setLoop(value); },
                    get normalizedTime() { return binding.getNormalizedTime(); },
                    play: () => binding.play(),
                    playState: (name) => binding.playState(name),
                    stop: () => binding.stop(),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createTilemap2DProxy(Tilemap2DBinding tilemap) {
        Value javaBinding = Value.asValue(tilemap);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get tilesetTextureGuid() { return binding.getTilesetTextureGuid(); },
                    set tilesetTextureGuid(value) { binding.setTilesetTextureGuid(value); },
                    getTile: (layer, x, y) => binding.getTile(layer, x, y),
                    setTile: (layer, x, y, guid) => binding.setTile(layer, x, y, guid),
                    refresh: (layer, x, y, radius) => binding.refresh(layer, x, y, radius == null ? 0 : radius),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createUILabelProxy(UILabelBinding label) {
        Value javaBinding = Value.asValue(label);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get text() { return binding.getText(); },
                    set text(value) { binding.setText(value); },
                  };
                })
                """).execute(javaBinding);
    }

    private Value createUIButtonProxy(UIButtonBinding button) {
        Value javaBinding = Value.asValue(button);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get label() { return binding.getLabel(); },
                    set label(value) { binding.setLabel(value); },
                    get interactable() { return binding.isInteractable(); },
                    set interactable(value) { binding.setInteractable(value); },
                    get hovered() { return binding.isHovered(); },
                    get pressed() { return binding.isPressed(); },
                    get clicked() { return binding.isClicked(); },
                  };
                })
                """).execute(javaBinding);
    }

    private Value createUIToggleProxy(UIToggleBinding toggle) {
        Value javaBinding = Value.asValue(toggle);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get label() { return binding.getLabel(); },
                    set label(value) { binding.setLabel(value); },
                    get isOn() { return binding.isOn(); },
                    set isOn(value) { binding.setOn(value); },
                    get interactable() { return binding.isInteractable(); },
                    set interactable(value) { binding.setInteractable(value); },
                  };
                })
                """).execute(javaBinding);
    }

    private Value createUITextFieldProxy(UITextFieldBinding field) {
        Value javaBinding = Value.asValue(field);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get value() { return binding.getValue(); },
                    set value(v) { binding.setValue(v); },
                    get focused() { return binding.isFocused(); },
                    get interactable() { return binding.isInteractable(); },
                    set interactable(v) { binding.setInteractable(v); },
                    setFocus: (focus) => binding.setFocus(focus),
                  };
                })
                """).execute(javaBinding);
    }

    private Value createSpriteRendererProxy(SpriteRendererBinding sprite) {
        Value javaBinding = Value.asValue(sprite);
        Value colorProxy = valueFactory.createColorProxy(sprite.getColor());
        return context.eval("js", """
                (function (binding, color) {
                  return {
                    get spriteGuid() { return binding.getSpriteGuid(); },
                    set spriteGuid(value) { binding.setSpriteGuid(value); },
                    get textureGuid() { return binding.getTextureGuid(); },
                    set textureGuid(value) { binding.setTextureGuid(value); },
                    get color() { return color; },
                    get sortingOrder() { return binding.getSortingOrder(); },
                    set sortingOrder(value) { binding.setSortingOrder(value); },
                  };
                })
                """).execute(javaBinding, colorProxy);
    }

    /** Hook for per-frame binding refresh (time and input read live bridges). */
    public void refreshFrameBindings() {
        // Time and Input read live values from PlayClock / PlayInputBridge.
    }

    /**
     * @return Graal host access rules for exported bindings
     */
    public static HostAccess hostAccess() {
        return HostAccess.newBuilder()
                .allowAccessAnnotatedBy(HostAccess.Export.class)
                .allowListAccess(true)
                .allowMapAccess(true)
                .allowArrayAccess(true)
                .build();
    }
}
