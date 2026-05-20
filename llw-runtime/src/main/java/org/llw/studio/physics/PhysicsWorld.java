package org.llw.studio.physics;

import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.PhysicsBodyRefComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.GameObject;
import org.llw.studio.serialization.PrefabSerializer;
import org.llw.studio.systems.TransformSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Box2D world wrapper for play mode.
 */
public final class PhysicsWorld {
    /** Studio units are pixels; Box2D is tuned for meter-scale, so gravity is scaled up. */
    public static final float PIXELS_PER_METER = 100f;
    public static final float DEFAULT_GRAVITY_Y = 9.81f * PIXELS_PER_METER;
    private static final float DEFAULT_DENSITY = 1f;

    private final org.jbox2d.dynamics.World box2d;
    private final Map<EntityId, Body> bodies = new HashMap<>();
    private final List<PhysicsContactEvent> contactEvents = new ArrayList<>();
    private final Set<Long> activeContacts = new HashSet<>();
    private final Set<Long> previousContacts = new HashSet<>();

    private float gravityX;
    private float gravityY = DEFAULT_GRAVITY_Y;
    private World ecsWorld;

    public PhysicsWorld() {
        this.box2d = new org.jbox2d.dynamics.World(PhysicsCoordinates.studioToBox2d(0f, DEFAULT_GRAVITY_Y));
        this.box2d.setContactListener(createContactListener());
        this.box2d.setContactFilter(createContactFilter());
    }

    public float gravityX() {
        return gravityX;
    }

    public float gravityY() {
        return gravityY;
    }

    public void setGravity(float studioX, float studioY) {
        this.gravityX = studioX;
        this.gravityY = studioY;
        box2d.setGravity(PhysicsCoordinates.studioVectorToBox2d(studioX, studioY));
    }

    public org.jbox2d.dynamics.World box2dWorld() {
        return box2d;
    }

    public Body bodyFor(EntityId entity) {
        return bodies.get(entity);
    }

    public EntityId entityForBody(Body body) {
        if (body == null || body.getUserData() == null) {
            return EntityId.none();
        }
        return ((PhysicsBodyUserData) body.getUserData()).entityId;
    }

    public List<PhysicsContactEvent> drainContactEvents() {
        List<PhysicsContactEvent> copy = List.copyOf(contactEvents);
        contactEvents.clear();
        return copy;
    }

    public void buildFromScene(World world) {
        destroy();
        ecsWorld = world;
        TransformSystem transformSystem = new TransformSystem();
        for (int pass = 0; pass < 4; pass++) {
            transformSystem.onUpdate(world, 0f);
        }

        var transforms = world.store(Transform2DComponent.class);
        for (int i = 0; i < transforms.size(); i++) {
            EntityId entity = transforms.entityAt(i);
            if (!hasPhysicsComponents(world, entity)) {
                continue;
            }
            if (!ActiveUtility.isEffectivelyActive(world, entity)) {
                continue;
            }
            createBodyForEntity(world, entity);
        }
        primeContactCallbacks();
        if (bodies.isEmpty() && countPhysicsEntities(world) > 0) {
            org.llw.util.log.Log.get("llw.studio.physics").warn(
                    "No Box2D bodies were created; ensure objects are active and have colliders"
            );
        }
    }

    private static int countPhysicsEntities(World world) {
        int count = 0;
        var transforms = world.store(Transform2DComponent.class);
        for (int i = 0; i < transforms.size(); i++) {
            if (hasPhysicsComponents(world, transforms.entityAt(i))) {
                count++;
            }
        }
        return count;
    }

    public void destroy() {
        for (Body body : bodies.values()) {
            box2d.destroyBody(body);
        }
        bodies.clear();
        contactEvents.clear();
        activeContacts.clear();
        previousContacts.clear();
        ecsWorld = null;
    }

    /**
     * Creates a Box2D body for a single entity spawned after play-mode physics was built
     * (for example via {@code Scene.createEntity(prefabGuid)}).
     */
    public void registerEntity(World world, EntityId entity) {
        if (world == null || entity.isNone()) {
            return;
        }
        ecsWorld = world;
        new TransformSystem().onUpdate(world, 0f);
        if (!hasPhysicsComponents(world, entity)) {
            return;
        }
        if (!ActiveUtility.isEffectivelyActive(world, entity)) {
            return;
        }
        if (bodies.containsKey(entity)) {
            return;
        }
        createBodyForEntity(world, entity);
    }

    /**
     * Registers physics bodies for {@code root} and every descendant in hierarchy order.
     */
    public void registerSubtree(World world, GameObject root) {
        if (world == null || root == null) {
            return;
        }
        ecsWorld = world;
        List<GameObject> objects = PrefabSerializer.collectSubtree(root);
        TransformSystem transformSystem = new TransformSystem();
        for (int pass = 0; pass < 2; pass++) {
            transformSystem.onUpdate(world, 0f);
        }
        for (GameObject object : objects) {
            registerEntity(world, object.entity());
        }
    }

    /**
     * Removes the Box2D body for an entity destroyed at runtime.
     */
    public void unregisterEntity(EntityId entity) {
        if (entity.isNone()) {
            return;
        }
        Body body = bodies.remove(entity);
        if (body == null) {
            return;
        }
        box2d.destroyBody(body);
        if (ecsWorld != null) {
            PhysicsBodyRefComponent ref = ecsWorld.getComponent(entity, PhysicsBodyRefComponent.class);
            if (ref != null) {
                ref.body = null;
            }
        }
        purgeContactsFor(entity);
    }

    private void purgeContactsFor(EntityId entity) {
        activeContacts.removeIf(key -> contactInvolves(key, entity));
        previousContacts.removeIf(key -> contactInvolves(key, entity));
    }

    private static boolean contactInvolves(long key, EntityId entity) {
        ContactPair pair = decodeContactKey(key);
        return pair.entityA().equals(entity) || pair.entityB().equals(entity);
    }

    public void step(float dt) {
        previousContacts.clear();
        previousContacts.addAll(activeContacts);
        activeContacts.clear();
        wakeAllBodies();
        box2d.step(dt, 8, 3);
        pollOngoingContacts();
        pollManualOverlaps();
        emitStayEvents();
        emitExitEvents();
    }

    public void syncTransformsToBodies(World world) {
        for (Map.Entry<EntityId, Body> entry : bodies.entrySet()) {
            EntityId entity = entry.getKey();
            Body body = entry.getValue();
            PhysicsBodyRefComponent ref = world.getComponent(entity, PhysicsBodyRefComponent.class);
            Rigidbody2DComponent rb = world.getComponent(entity, Rigidbody2DComponent.class);
            boolean simulated = rb == null || rb.simulated;
            boolean kinematicDrive = simulated
                    && rb != null
                    && rb.bodyType == PhysicsBodyType.KINEMATIC;
            if (ref == null || (!ref.transformDirty && !kinematicDrive)) {
                continue;
            }
            Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
            WorldTransformComponent wt = world.getComponent(entity, WorldTransformComponent.class);
            if (transform == null || wt == null) {
                continue;
            }
            Vec2 pos = PhysicsCoordinates.studioToBox2d(wt.worldX, wt.worldY);
            float angle = PhysicsCoordinates.studioToBox2dAngleRadians(wt.worldRotation);
            if (rb != null && rb.bodyType == PhysicsBodyType.KINEMATIC) {
                Vec2 previous = body.getPosition().clone();
                body.setTransform(pos, angle);
                Vec2 delta = pos.sub(previous);
                float invDt = 1f / org.llw.studio.systems.PhysicsSystem.FIXED_DT;
                body.setLinearVelocity(new Vec2(delta.x * invDt, delta.y * invDt));
                body.setAngularVelocity(0f);
                body.setAwake(true);
            } else {
                body.setTransform(pos, angle);
            }
            ref.transformDirty = false;
        }
    }

    public void syncBodiesToTransforms(World world) {
        for (Map.Entry<EntityId, Body> entry : bodies.entrySet()) {
            EntityId entity = entry.getKey();
            Body body = entry.getValue();
            PhysicsBodyRefComponent ref = world.getComponent(entity, PhysicsBodyRefComponent.class);
            Rigidbody2DComponent rb = world.getComponent(entity, Rigidbody2DComponent.class);
            if (rb == null || rb.bodyType != PhysicsBodyType.DYNAMIC) {
                continue;
            }
            if (ref != null && !ref.syncTransformFromBody) {
                continue;
            }
            Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
            if (local == null) {
                continue;
            }
            HierarchyComponentCheck(world, entity, body, local, rb);
        }
    }

    private void HierarchyComponentCheck(
            World world,
            EntityId entity,
            Body body,
            Transform2DComponent local,
            Rigidbody2DComponent rb
    ) {
        Vec2 pos = body.getPosition();
        float angle = body.getAngle();
        var hierarchy = world.getComponent(entity, org.llw.studio.ecs.components.HierarchyComponent.class);
        if (hierarchy == null || hierarchy.parentIndex < 0) {
            local.x = PhysicsCoordinates.studioXFromBox2d(pos.x);
            local.y = PhysicsCoordinates.studioYFromBox2d(pos.y);
            local.rotation = PhysicsCoordinates.box2dToStudioAngleDegrees(angle);
        } else {
            EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
            WorldTransformComponent parentWorld = world.getComponent(parentId, WorldTransformComponent.class);
            if (parentWorld != null) {
                writeLocalFromWorld(local, parentWorld, pos, angle);
            } else {
                local.x = PhysicsCoordinates.studioXFromBox2d(pos.x);
                local.y = PhysicsCoordinates.studioYFromBox2d(pos.y);
                local.rotation = PhysicsCoordinates.box2dToStudioAngleDegrees(angle);
            }
        }
        Vec2 vel = body.getLinearVelocity();
        Vec2 studioVel = PhysicsCoordinates.box2dVectorToStudio(vel);
        rb.linearVelocityX = studioVel.x;
        rb.linearVelocityY = studioVel.y;
        rb.angularVelocity = PhysicsCoordinates.box2dToStudioAngleDegrees(body.getAngularVelocity());
    }

    private static void writeLocalFromWorld(
            Transform2DComponent local,
            WorldTransformComponent parentWorld,
            Vec2 worldPosBox2d,
            float worldAngleBox2d
    ) {
        float worldX = PhysicsCoordinates.studioXFromBox2d(worldPosBox2d.x);
        float worldY = PhysicsCoordinates.studioYFromBox2d(worldPosBox2d.y);
        float worldRot = PhysicsCoordinates.box2dToStudioAngleDegrees(worldAngleBox2d);
        float dx = worldX - parentWorld.worldX;
        float dy = worldY - parentWorld.worldY;
        float parentRad = (float) Math.toRadians(parentWorld.worldRotation);
        float cos = (float) Math.cos(-parentRad);
        float sin = (float) Math.sin(-parentRad);
        float invScaleX = parentWorld.worldScaleX == 0f ? 1f : 1f / parentWorld.worldScaleX;
        float invScaleY = parentWorld.worldScaleY == 0f ? 1f : 1f / parentWorld.worldScaleY;
        local.x = (dx * cos - dy * sin) * invScaleX;
        local.y = (dx * sin + dy * cos) * invScaleY;
        local.rotation = worldRot - parentWorld.worldRotation;
    }

    public PhysicsRaycastHit raycast(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float distance,
            int layerMask
    ) {
        if (distance <= 0f) {
            return null;
        }
        Vec2 origin = PhysicsCoordinates.studioToBox2d(originX, originY);
        Vec2 dir = PhysicsCoordinates.studioVectorToBox2d(directionX, directionY);
        if (dir.lengthSquared() < 1e-8f) {
            return null;
        }
        dir.normalize();
        Vec2 end = origin.add(dir.mul(distance));
        RaycastResult result = new RaycastResult();
        box2d.raycast(new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
                if (!fixtureMatchesMask(fixture, layerMask)) {
                    return -1f;
                }
                if (fraction < result.fraction) {
                    result.fraction = fraction;
                    result.fixture = fixture;
                    result.point = point.clone();
                    result.normal = normal.clone();
                }
                return fraction;
            }
        }, origin, end);
        if (result.fixture == null) {
            return null;
        }
        EntityId entity = entityForBody(result.fixture.getBody());
        Vec2 studioPoint = PhysicsCoordinates.box2dVectorToStudio(result.point);
        Vec2 studioNormal = PhysicsCoordinates.box2dVectorToStudio(result.normal);
        return new PhysicsRaycastHit(
                entity,
                studioPoint.x,
                studioPoint.y,
                studioNormal.x,
                studioNormal.y,
                result.fraction
        );
    }

    public List<EntityId> overlapCircle(float centerX, float centerY, float radius, int layerMask) {
        List<EntityId> hits = new ArrayList<>();
        if (radius <= 0f) {
            return hits;
        }
        Vec2 center = PhysicsCoordinates.studioToBox2d(centerX, centerY);
        Vec2 lower = center.sub(new Vec2(radius, radius));
        Vec2 upper = center.add(new Vec2(radius, radius));
        Set<EntityId> seen = new HashSet<>();
        AABB aabb = new AABB();
        aabb.lowerBound.set(lower);
        aabb.upperBound.set(upper);
        box2d.queryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (!fixtureMatchesMask(fixture, layerMask)) {
                    return true;
                }
                EntityId entity = entityForBody(fixture.getBody());
                if (!entity.isNone() && seen.add(entity)) {
                    hits.add(entity);
                }
                return true;
            }
        }, aabb);
        return hits;
    }

    private boolean fixtureMatchesMask(Fixture fixture, int layerMask) {
        int category = fixture.getFilterData().categoryBits;
        if (category == 0) {
            return true;
        }
        int layer = Integer.numberOfTrailingZeros(category);
        return (layerMask & (1 << layer)) != 0;
    }

    private void createBodyForEntity(World world, EntityId entity) {
        Rigidbody2DComponent rb = world.getComponent(entity, Rigidbody2DComponent.class);
        boolean useRigidbody = rb != null && rb.simulated;
        PhysicsBodyType type = !useRigidbody ? PhysicsBodyType.STATIC : rb.bodyType;

        WorldTransformComponent wt = world.getComponent(entity, WorldTransformComponent.class);
        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
        float worldX = wt != null ? wt.worldX : local.x;
        float worldY = wt != null ? wt.worldY : local.y;
        float worldRot = wt != null ? wt.worldRotation : local.rotation;

        BodyDef def = new BodyDef();
        def.type = toBox2dType(type);
        def.position.set(PhysicsCoordinates.studioToBox2d(worldX, worldY));
        def.angle = PhysicsCoordinates.studioToBox2dAngleRadians(worldRot);
        def.fixedRotation = useRigidbody && rb.freezeRotation;

        Body body = box2d.createBody(def);
        body.setUserData(new PhysicsBodyUserData(entity));
        bodies.put(entity, body);

        PhysicsBodyRefComponent ref = world.getComponent(entity, PhysicsBodyRefComponent.class);
        if (ref == null) {
            ref = new PhysicsBodyRefComponent();
            world.addComponent(entity, PhysicsBodyRefComponent.class, ref);
        }
        ref.body = body;
        ref.syncTransformFromBody = type == PhysicsBodyType.DYNAMIC;

        BoxCollider2DComponent box = world.getComponent(entity, BoxCollider2DComponent.class);
        if (box != null) {
            attachBoxFixture(body, box);
        }
        CircleCollider2DComponent circle = world.getComponent(entity, CircleCollider2DComponent.class);
        if (circle != null) {
            attachCircleFixture(body, circle);
        }
        EdgeCollider2DComponent edge = world.getComponent(entity, EdgeCollider2DComponent.class);
        if (edge != null) {
            attachEdgeFixtures(body, edge);
        }

        if (useRigidbody && type == PhysicsBodyType.DYNAMIC) {
            body.setGravityScale(rb.gravityScale);
            body.setLinearDamping(rb.linearDrag);
            body.setAngularDamping(rb.angularDrag);
            if (rb.mass > 0f) {
                body.setMassData(massDataFor(rb.mass, body));
            }
        }
        ref.transformDirty = true;
    }

    /**
     * Runs one physics step after scene build so touching pairs receive enter callbacks
     * (Box2D does not call {@code beginContact} for pairs that start overlapped until a step runs).
     */
    private void primeContactCallbacks() {
        if (bodies.isEmpty()) {
            return;
        }
        previousContacts.clear();
        activeContacts.clear();
        step(1f / 50f);
    }

    /**
     * Ensures touching pairs are tracked when Box2D skips {@code beginContact}
     * (common for kinematic bodies moved via {@code setTransform}).
     */
    private void wakeAllBodies() {
        for (Body body : bodies.values()) {
            body.setAwake(true);
        }
    }

    private void pollOngoingContacts() {
        Contact contact = box2d.getContactList();
        while (contact != null) {
            if (contact.isTouching()) {
                handleContact(contact, true);
            }
            contact = contact.getNext();
        }
    }

    private static org.jbox2d.collision.shapes.MassData massDataFor(float mass, Body body) {
        org.jbox2d.collision.shapes.MassData data = new org.jbox2d.collision.shapes.MassData();
        body.getMassData(data);
        data.mass = mass;
        return data;
    }

    private void attachBoxFixture(Body body, BoxCollider2DComponent box) {
        PolygonShape shape = new PolygonShape();
        float hx = Math.max(0.001f, box.sizeX * 0.5f);
        float hy = Math.max(0.001f, box.sizeY * 0.5f);
        Vec2 center = PhysicsCoordinates.studioToBox2d(box.offsetX, box.offsetY);
        shape.setAsBox(hx, hy, center, 0f);
        attachFixture(body, shape, box.isTrigger, box.layer, box.layerMask);
    }

    private void attachCircleFixture(Body body, CircleCollider2DComponent circle) {
        CircleShape shape = new CircleShape();
        shape.m_radius = Math.max(0.001f, circle.radius);
        Vec2 center = PhysicsCoordinates.studioToBox2d(circle.offsetX, circle.offsetY);
        shape.m_p.set(center);
        attachFixture(body, shape, circle.isTrigger, circle.layer, circle.layerMask);
    }

    private void attachEdgeFixtures(Body body, EdgeCollider2DComponent edge) {
        float[] pts = edge.points;
        if (pts == null || pts.length < 4) {
            return;
        }
        int count = pts.length / 2;
        if (count == 2) {
            EdgeShape shape = new EdgeShape();
            shape.set(
                    PhysicsCoordinates.studioToBox2d(pts[0], pts[1]),
                    PhysicsCoordinates.studioToBox2d(pts[2], pts[3])
            );
            attachFixture(body, shape, edge.isTrigger, edge.layer, edge.layerMask);
            return;
        }
        ChainShape chain = new ChainShape();
        Vec2[] vertices = new Vec2[count];
        for (int i = 0; i < count; i++) {
            vertices[i] = PhysicsCoordinates.studioToBox2d(pts[i * 2], pts[i * 2 + 1]);
        }
        chain.createChain(vertices, count);
        attachFixture(body, chain, edge.isTrigger, edge.layer, edge.layerMask);
    }

    private void attachFixture(Body body, org.jbox2d.collision.shapes.Shape shape, boolean sensor, int layer, int layerMask) {
        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = DEFAULT_DENSITY;
        def.isSensor = sensor;
        def.filter = PhysicsLayerFilter.createFilter(layer, layerMask);
        body.createFixture(def);
    }

    private static BodyType toBox2dType(PhysicsBodyType type) {
        return switch (type) {
            case STATIC -> BodyType.STATIC;
            case KINEMATIC -> BodyType.KINEMATIC;
            case DYNAMIC -> BodyType.DYNAMIC;
        };
    }

    private static boolean hasPhysicsComponents(World world, EntityId entity) {
        return world.getComponent(entity, Rigidbody2DComponent.class) != null
                || world.getComponent(entity, BoxCollider2DComponent.class) != null
                || world.getComponent(entity, CircleCollider2DComponent.class) != null
                || world.getComponent(entity, EdgeCollider2DComponent.class) != null;
    }

    private ContactListener createContactListener() {
        return new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                handleContact(contact, true);
            }

            @Override
            public void endContact(Contact contact) {
                handleContact(contact, false);
            }

            @Override
            public void preSolve(Contact contact, org.jbox2d.collision.Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        };
    }

    private void handleContact(Contact contact, boolean begin) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        EntityId entityA = entityForBody(a.getBody());
        EntityId entityB = entityForBody(b.getBody());
        if (entityA.isNone() || entityB.isNone()) {
            return;
        }
        boolean trigger = a.isSensor() || b.isSensor();
        long key = contactKey(entityA, entityB, trigger);
        if (begin) {
            boolean wasActive = activeContacts.contains(key);
            activeContacts.add(key);
            if (!previousContacts.contains(key) && !wasActive) {
                queueContact(trigger ? PhysicsMessageType.TRIGGER_ENTER : PhysicsMessageType.COLLISION_ENTER,
                        entityA, entityB, contact);
                queueContact(trigger ? PhysicsMessageType.TRIGGER_ENTER : PhysicsMessageType.COLLISION_ENTER,
                        entityB, entityA, contact);
            }
        } else {
            activeContacts.remove(key);
        }
    }

    private void handleOverlapPair(EntityId entityA, EntityId entityB, boolean trigger) {
        long key = contactKey(entityA, entityB, trigger);
        boolean wasActive = activeContacts.contains(key);
        activeContacts.add(key);
        if (!previousContacts.contains(key) && !wasActive) {
            queueContactSimple(
                    trigger ? PhysicsMessageType.TRIGGER_ENTER : PhysicsMessageType.COLLISION_ENTER,
                    entityA,
                    entityB,
                    trigger
            );
            queueContactSimple(
                    trigger ? PhysicsMessageType.TRIGGER_ENTER : PhysicsMessageType.COLLISION_ENTER,
                    entityB,
                    entityA,
                    trigger
            );
        }
    }

    private void queueContactSimple(PhysicsMessageType type, EntityId self, EntityId other, boolean trigger) {
        contactEvents.add(new PhysicsContactEvent(type, self, other, trigger, 0f, 0f));
    }

    private void emitExitEvents() {
        for (Long key : previousContacts) {
            if (activeContacts.contains(key)) {
                continue;
            }
            ContactPair pair = decodeContactKey(key);
            queueContactSimple(
                    pair.trigger ? PhysicsMessageType.TRIGGER_EXIT : PhysicsMessageType.COLLISION_EXIT,
                    pair.entityA,
                    pair.entityB,
                    pair.trigger
            );
            queueContactSimple(
                    pair.trigger ? PhysicsMessageType.TRIGGER_EXIT : PhysicsMessageType.COLLISION_EXIT,
                    pair.entityB,
                    pair.entityA,
                    pair.trigger
            );
        }
    }

    /**
     * Box2D does not create contacts for kinematic-kinematic (and some kinematic-static) pairs.
     * Overlap is detected with fixture AABBs so script callbacks still fire.
     */
    private void pollManualOverlaps() {
        List<Body> bodyList = new ArrayList<>(bodies.values());
        for (int i = 0; i < bodyList.size(); i++) {
            Body bodyA = bodyList.get(i);
            for (int j = i + 1; j < bodyList.size(); j++) {
                Body bodyB = bodyList.get(j);
                if (!needsManualOverlapTracking(bodyA, bodyB)) {
                    continue;
                }
                pollFixtureOverlaps(bodyA, bodyB);
            }
        }
    }

    private static boolean needsManualOverlapTracking(Body bodyA, Body bodyB) {
        BodyType typeA = bodyA.getType();
        BodyType typeB = bodyB.getType();
        if (typeA == BodyType.KINEMATIC && typeB == BodyType.KINEMATIC) {
            return true;
        }
        if (typeA == BodyType.KINEMATIC && typeB == BodyType.STATIC) {
            return true;
        }
        return typeA == BodyType.STATIC && typeB == BodyType.KINEMATIC;
    }

    private void pollFixtureOverlaps(Body bodyA, Body bodyB) {
        for (Fixture fixtureA = bodyA.getFixtureList(); fixtureA != null; fixtureA = fixtureA.getNext()) {
            for (Fixture fixtureB = bodyB.getFixtureList(); fixtureB != null; fixtureB = fixtureB.getNext()) {
                if (!fixturesCanCollide(fixtureA, fixtureB)) {
                    continue;
                }
                if (!fixturesOverlap(fixtureA, fixtureB)) {
                    continue;
                }
                EntityId entityA = entityForBody(bodyA);
                EntityId entityB = entityForBody(bodyB);
                if (entityA.isNone() || entityB.isNone()) {
                    continue;
                }
                boolean trigger = fixtureA.isSensor() || fixtureB.isSensor();
                handleOverlapPair(entityA, entityB, trigger);
            }
        }
    }

    private static boolean fixturesCanCollide(Fixture fixtureA, Fixture fixtureB) {
        int layerA = Integer.numberOfTrailingZeros(fixtureA.getFilterData().categoryBits);
        int layerB = Integer.numberOfTrailingZeros(fixtureB.getFilterData().categoryBits);
        return PhysicsLayerFilter.canCollide(
                layerA,
                fixtureA.getFilterData().maskBits,
                layerB,
                fixtureB.getFilterData().maskBits
        );
    }

    private static boolean fixturesOverlap(Fixture fixtureA, Fixture fixtureB) {
        AABB aabbA = fixtureA.getAABB(0);
        AABB aabbB = fixtureB.getAABB(0);
        return aabbA.lowerBound.x <= aabbB.upperBound.x
                && aabbA.upperBound.x >= aabbB.lowerBound.x
                && aabbA.lowerBound.y <= aabbB.upperBound.y
                && aabbA.upperBound.y >= aabbB.lowerBound.y;
    }

    private void emitStayEvents() {
        for (Long key : activeContacts) {
            if (!previousContacts.contains(key)) {
                continue;
            }
            ContactPair pair = decodeContactKey(key);
            queueStay(pair.trigger, pair.entityA, pair.entityB);
            queueStay(pair.trigger, pair.entityB, pair.entityA);
        }
    }

    private void queueStay(boolean trigger, EntityId self, EntityId other) {
        contactEvents.add(new PhysicsContactEvent(
                trigger ? PhysicsMessageType.TRIGGER_STAY : PhysicsMessageType.COLLISION_STAY,
                self,
                other,
                trigger,
                0f,
                0f
        ));
    }

    private void queueContact(PhysicsMessageType type, EntityId self, EntityId other, Contact contact) {
        Vec2 relative = contact.getFixtureA().getBody().getLinearVelocity()
                .sub(contact.getFixtureB().getBody().getLinearVelocity());
        Vec2 studioRel = PhysicsCoordinates.box2dVectorToStudio(relative);
        boolean trigger = type == PhysicsMessageType.TRIGGER_ENTER
                || type == PhysicsMessageType.TRIGGER_STAY
                || type == PhysicsMessageType.TRIGGER_EXIT;
        contactEvents.add(new PhysicsContactEvent(
                type,
                self,
                other,
                trigger,
                studioRel.x,
                studioRel.y
        ));
    }

    private static long contactKey(EntityId a, EntityId b, boolean trigger) {
        int ai = a.index();
        int bi = b.index();
        int min = Math.min(ai, bi);
        int max = Math.max(ai, bi);
        return ((long) min << 32) | (max & 0xFFFF_FFFFL) | (trigger ? 1L << 63 : 0L);
    }

    private static ContactPair decodeContactKey(long key) {
        boolean trigger = (key & (1L << 63)) != 0;
        int min = (int) (key >> 32);
        int max = (int) key;
        return new ContactPair(new EntityId(min, 0), new EntityId(max, 0), trigger);
    }

    private ContactFilter createContactFilter() {
        return new ContactFilter() {
            @Override
            public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
                int layerA = Integer.numberOfTrailingZeros(fixtureA.getFilterData().categoryBits);
                int layerB = Integer.numberOfTrailingZeros(fixtureB.getFilterData().categoryBits);
                return PhysicsLayerFilter.canCollide(
                        layerA,
                        fixtureA.getFilterData().maskBits,
                        layerB,
                        fixtureB.getFilterData().maskBits
                );
            }
        };
    }

    private static final class RaycastResult {
        float fraction = 1f;
        Fixture fixture;
        Vec2 point = new Vec2();
        Vec2 normal = new Vec2();
    }

    private record ContactPair(EntityId entityA, EntityId entityB, boolean trigger) {
    }
}
