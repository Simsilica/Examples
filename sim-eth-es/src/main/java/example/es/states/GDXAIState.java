/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es.states;

import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath.LinePathParam;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.MobPath;
import example.es.Position;
import example.es.SteeringSeekable;
import example.es.Steerable;
import example.es.SteeringPath;
import example.es.SteeringSeek;
import example.sim.GameEntities;
import example.sim.GDXAIDriver;
import example.sim.PhysicsListener;
import example.sim.SimpleBody;
import example.sim.SimplePhysics;
import java.util.HashMap;
import java.util.Map;
import org.dyn4j.dynamics.Force;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Asser
 */
public class GDXAIState extends AbstractGameSystem implements PhysicsListener {

    private EntityData ed;
    private SimTime tpf;
    private HashMap<Entity, GDXAIDriver> drivers = new HashMap<>();
    private MobSteerables mobSteerables;
    private SimplePhysics simplePhysics;
    private MobSeekables mobSeekables;
    private MobSeekers mobSeekers;
    private MobPathers mobPathers;

    static Logger log = LoggerFactory.getLogger(GDXAIState.class);

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        simplePhysics = getSystem(SimplePhysics.class);
    }

    @Override
    protected void terminate() {
    }

    @Override
    public void start() {

        mobSeekables = new MobSeekables(ed);
        mobSeekables.start();

        mobSteerables = new MobSteerables(ed);
        mobSteerables.start();

        mobSeekers = new MobSeekers(ed);
        mobSeekers.start();

        mobPathers = new MobPathers(ed);
        mobPathers.start();
    }

    @Override
    public void stop() {

        mobPathers.stop();
        mobPathers = null;

        mobSeekers.stop();
        mobSeekers = null;

        mobSteerables.stop();
        mobSteerables = null;

        mobSeekables.stop();
        mobSeekables = null;
    }

    @Override
    public void update(SimTime tpf) {
        //Get a local copy for use in looking up positions in the bodyposition
        this.tpf = tpf;

        mobSeekables.update();
        mobSteerables.update();

        mobSeekers.update();
        mobSeekers.createAndApplySteeringForce(tpf);

        mobPathers.update();
        mobPathers.createAndApplySteeringForce(tpf);
    }

    @Override
    public void beginFrame(SimTime time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addBody(SimpleBody body) {
    }

    @Override
    public void updateBody(SimpleBody body) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeBody(SimpleBody body) {
    }

    @Override
    public void endFrame(SimTime time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Map the mobs to a steerable we can apply calculateSteering on (depending on the behaviour)
    private class MobSteerables extends EntityContainer<GDXAIDriver> {

        public MobSteerables(EntityData ed) {
            super(ed, Steerable.class);
        }

        @Override
        protected GDXAIDriver[] getArray() {
            return super.getArray();
        }

        @Override
        protected GDXAIDriver addObject(Entity e) {
            SimpleBody body = simplePhysics.getBody(e.getId());
            GDXAIDriver driver = new GDXAIDriver(body);
            body.driver = driver;

            drivers.put(e, driver);
            //log.info("Creating driver:" + driver.toString());
            //log.info("On body: "+body.toString());
            return driver;
        }

        @Override
        protected void updateObject(GDXAIDriver object, Entity e) {
        }

        @Override
        protected void removeObject(GDXAIDriver object, Entity e) {
            drivers.remove(e);
        }
    }

    //Map entities to seeking behaviours
    private class MobSeekers extends EntityContainer<SteeringBehavior> {

        private HashMap<EntityId, SteeringBehavior<com.badlogic.gdx.math.Vector2>> mobSeekerMap = new HashMap<>();

        public MobSeekers(EntityData ed) {
            super(ed, SteeringSeek.class);
        }

        @Override
        protected SteeringBehavior[] getArray() {
            return super.getArray();
        }

        @Override
        protected SteeringBehavior addObject(Entity e) {
            SteeringSeek ss = e.get(SteeringSeek.class);

            //The driver to create a behaviour for
            GDXAIDriver steerable = mobSteerables.getObject(e.getId());

            //The target location
            Entity targetEntity = ed.getEntity(ss.getTarget(), Position.class);
            Dyn4jLocation seekable = mobSeekables.getObject(targetEntity.getId());

            SteeringBehavior<com.badlogic.gdx.math.Vector2> behaviour = new Seek(steerable, seekable);
            behaviour.setLimiter(steerable);

            mobSeekerMap.put(e.getId(), behaviour);

            //log.info("Creating behaviour:" + behaviour.toString());
            return behaviour;
        }

        @Override
        protected void updateObject(SteeringBehavior object, Entity e) {
        }

        @Override
        protected void removeObject(SteeringBehavior object, Entity e) {
            mobSeekerMap.remove(e.getId());
        }

        public void createAndApplySteeringForce(SimTime tpf) {

            mobSeekerMap.entrySet()
                    .forEach((Map.Entry<EntityId, SteeringBehavior<Vector2>> entry) -> {
                        SteeringAcceleration<Vector2> steeringOutput
                                = new SteeringAcceleration<>(new Vector2());

                        EntityId eId = entry.getKey();
                        SteeringBehavior<Vector2> behaviour = entry.getValue();

                        behaviour.calculateSteering(steeringOutput);

                        Vector3f thrust = new Vector3f((float) steeringOutput.linear.x, (float) steeringOutput.linear.y, 0);

                        SimpleBody b = simplePhysics.getBody(eId);
                        ((GDXAIDriver) b.driver).applyMovementState(thrust, Vector3f.ZERO);
                    }
                    );
        }
    }

    private com.badlogic.gdx.math.Vector2 convert(org.dyn4j.geometry.Vector2 vec2) {
        return new com.badlogic.gdx.math.Vector2((float) vec2.x, (float) vec2.y);
    }

    private org.dyn4j.geometry.Vector2 convert(com.badlogic.gdx.math.Vector2 vec2) {
        return new org.dyn4j.geometry.Vector2((double) vec2.x, (double) vec2.y);

    }

    //Map entities to a location we can seek (ie. bases)
    private class MobSeekables extends EntityContainer<Dyn4jLocation> {

        public MobSeekables(EntityData ed) {
            super(ed, SteeringSeekable.class, Position.class);
        }

        @Override
        protected Dyn4jLocation[] getArray() {
            return super.getArray();
        }

        @Override
        protected Dyn4jLocation addObject(Entity e) {
            Dyn4jLocation loc = new Dyn4jLocation(e.getId());
            return loc;
        }

        @Override
        protected void updateObject(Dyn4jLocation object, Entity e) {
        }

        @Override
        protected void removeObject(Dyn4jLocation object, Entity e) {
            drivers.remove(e);
        }
    }

    //Map entities to pathing behaviours
    private class MobPathers extends EntityContainer<SteeringBehavior> {

        private HashMap<EntityId, SteeringBehavior<com.badlogic.gdx.math.Vector2>> mobPatherMap = new HashMap<>();

        public MobPathers(EntityData ed) {
            super(ed, SteeringPath.class, MobPath.class);
        }

        @Override
        protected SteeringBehavior[] getArray() {
            return super.getArray();
        }

        @Override
        protected SteeringBehavior addObject(Entity e) {
            MobPath path = e.get(MobPath.class);
            //The driver to create a behaviour for
            GDXAIDriver steerable = mobSteerables.getObject(e.getId());

            com.dongbat.walkable.FloatArray fa = path.getPath();
            com.badlogic.gdx.utils.Array<Vector2> a = new Array<>();

            for (int i = 0; i < fa.size; i = i + 2) {
                Vector2 vec = new Vector2(fa.get(i), fa.get(i + 1));
                a.add(vec);
            }

            LinePath<Vector2> linePath = new LinePath(a, true);
            SteeringBehavior<com.badlogic.gdx.math.Vector2> behaviour = new FollowPath<Vector2, LinePathParam>(steerable, linePath, 1)
                    .setTimeToTarget(0.1f)
                    .setArrivalTolerance(0.001f);
                    //.setDecelerationRadius(80);

            mobPatherMap.put(e.getId(), behaviour);
            return behaviour;
        }

        @Override
        protected void updateObject(SteeringBehavior object, Entity e) {
        }

        @Override
        protected void removeObject(SteeringBehavior object, Entity e) {
            mobPatherMap.remove(e.getId());
        }

        public void createAndApplySteeringForce(SimTime tpf) {
            mobPatherMap.entrySet()
                    .forEach((Map.Entry<EntityId, SteeringBehavior<Vector2>> entry) -> {
                        SteeringAcceleration<Vector2> steeringOutput
                                = new SteeringAcceleration<>(new Vector2());

                        EntityId eId = entry.getKey();
                        SteeringBehavior<Vector2> behaviour = entry.getValue();

                        behaviour.calculateSteering(steeringOutput);

                        Vector3f thrust = new Vector3f((float) steeringOutput.linear.x, (float) steeringOutput.linear.y, 0);

                        SimpleBody b = simplePhysics.getBody(eId);
                        ((GDXAIDriver) b.driver).applyMovementState(thrust, Vector3f.ZERO);
                    }
                    );
        }

    }

    protected class Dyn4jLocation implements Location<Vector2> {

        EntityId eId;

        public Dyn4jLocation(EntityId eId) {
            this.eId = eId;
        }

        @Override
        public Vector2 getPosition() {

            SimpleBody b = simplePhysics.getBody(eId);

            //log.info("Dyn4jLocation > getPosition");
            if (b != null) {

                return convert(b.getTransform().getTranslation());
            } else {
                Position p = ed.getComponent(eId, Position.class);
                return new com.badlogic.gdx.math.Vector2((float) p.getLocation().x, (float) p.getLocation().y);
            }
        }

        @Override
        public float getOrientation() {
            SimpleBody b = simplePhysics.getBody(eId);

            return b.getOrientation();
        }

        @Override
        public void setOrientation(float f) {
            SimpleBody b = simplePhysics.getBody(eId);

            b.setOrientation(f);
        }

        @Override
        public float vectorToAngle(Vector2 t) {
            return (float) Math.atan2(-t.x, t.y);
        }

        @Override
        public Vector2 angleToVector(Vector2 t, float f) {
            t.x = -(float) Math.sin(f);
            t.y = (float) Math.cos(f);
            return t;
        }

        @Override
        public Location<com.badlogic.gdx.math.Vector2> newLocation() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
