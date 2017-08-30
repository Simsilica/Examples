/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es.states;

import com.dongbat.walkable.FloatArray;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.es.Drivable;
import example.es.MobPath;
import example.sim.MobDriver;
import example.sim.SimpleBody;
import example.sim.SimplePhysics;
import java.util.HashMap;
import org.dyn4j.geometry.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Asser
 */
public class BasicSteeringState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet mobs;
    private SimTime tpf;
    private HashMap<EntityId, Integer> currentPathIndex = new HashMap<>();
    private HashMap<EntityId, Vector2> currentTargetMap = new HashMap<>();
    private SimplePhysics simplePhysics;
    private MobDrivers mobDrivers;
    private HashMap<Entity, MobDriver> drivers = new HashMap<>();
    static Logger log = LoggerFactory.getLogger(BasicSteeringState.class);

    @Override
    protected void initialize() {

        this.ed = getSystem(EntityData.class);

        //Get every entity that has a physical position and a path to follow
        this.mobs = ed.getEntities(MobPath.class);

        simplePhysics = getSystem(SimplePhysics.class);
    }

    @Override
    protected void terminate() {
        this.mobs.release();
        this.mobs = null;
    }

    @Override
    public void start() {
        mobDrivers = new MobDrivers(ed);
        mobDrivers.start();

    }

    @Override
    public void stop() {
        mobDrivers.stop();
        mobDrivers = null;
    }

    @Override
    public void update(SimTime tpf) {
        //Get a local copy for use in looking up positions in the bodyposition
        this.tpf = tpf;
        if (tpf.getTpf() < 1) {
            mobDrivers.updateDrivers();
        }
    }

    private boolean createAndApplySteeringForce(Entity e, SimTime tpf) {
        //This should be fun
        Vector2 steeringForce = new Vector2();
        if (steer(e, steeringForce)) {
            //log.info("(createSteeringForce) steeringForce: " + steeringForce.toString());
            //Drive the ship according to the steering force
            Vector3f thrust = new Vector3f((float) steeringForce.x, (float) steeringForce.y, 0);
            //log.info("(createSteeringForce) thrust: " + thrust.toString());
            SimpleBody b = simplePhysics.getBody(e.getId());
            //log.info("Position p:" + b.getTransform().getTranslation().toString() + ", thrust:" + thrust.toString());

            //TODO: Convert thrust to a correct steering vector (default is keyboard inputs, not a force vector)
            ((MobDriver) b.driver).applyMovementState(thrust);
            return true;
        }
        return false;
    }

    private float distance(Vector2 a, Vector2 b) {
        return (float) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    //Should be used whenever we need to move towards current target
    private int getCurrentPathIndex(EntityId eId) {
        if (currentPathIndex.containsKey(eId)) {
            return currentPathIndex.get(eId);
        } else {
            //Should not happen since we check on added and changed entities and call restartPath there
            currentPathIndex.put(eId, 0);
            return 0; //First index
        }
    }

    //Moves as 0, 2, 4.. because the path is x1,y1,x2,y2,x3,y3..
    private void setNextWayPoint(EntityId eId) {
        currentPathIndex.put(eId, currentPathIndex.get(eId) + 2);
    }

    //Should be called whenver MobPath component is updated on entity
    private void restartPath(EntityId eId) {
        currentPathIndex.put(eId, 0);
    }

    private Vector2 seek(Vector2 currentTarget, Vector2 currentPosition, Vector2 currentVelocity) {
        //TODO: Validate this calculation
        Vector2 force;

        Vector2 desired = currentTarget.subtract(currentPosition);
        desired.normalize();
        desired.multiply(GameConstants.MOBSPEED);
        //log.info("(seek), desired velocity: " + desired.toString());

        force = desired.subtract(currentVelocity);
        //log.info("(seek), force generated: " + force.toString());

        force = force.multiply(tpf.getTpf());
        //log.info("(seek) steeringForce after tpf: " + force.toString());
        return force;
    }

    private boolean steer(Entity e, Vector2 result) {
        Vector2 pathFollow = new Vector2();
        if (pathFollowing(e, pathFollow)) {
            //log.info("(steer) pathFollow: " + pathFollow.toString());
            result.add(pathFollow);
            //log.info("(steer) steering force: " + steeringForce.toString());
            result.setMagnitude(Math.min(GameConstants.MOBMAXFORCE, result.getMagnitude()));
            //log.info("(steer) steering force after magnitude: " + steeringForce.toString());
            return true;
        }
        return false;
    }

    //Sets the next waypoint into the result parameter, returns true if found, false if already reached end
    private boolean getWayPoint(FloatArray path, EntityId eId, Vector2 result) {
        boolean reachedEnd = true;

        int currentIndex = getCurrentPathIndex(eId);

        if (path.size > currentIndex) {
            float x = path.get(currentIndex);
            float y = path.get(currentIndex + 1);
            result.set(x, y);
            return true;
        }
        return false;
    }

    private Vector2 getCurrentTarget(Entity e) {
        return currentTargetMap.get(e.getId());
    }

    private boolean pathFollowing(Entity e, Vector2 result) {
        //Get current position
        Vector2 currentPosition = simplePhysics.getBody(e.getId()).getTransform().getTranslation();
        //Get current target
        MobPath mobPath = e.get(MobPath.class);
        FloatArray path = mobPath.getPath();

        Vector2 currentTarget = new Vector2();

        //Get current waypoint
        getWayPoint(path, e.getId(), currentTarget);
        //Check distance to current target node
        if (distance(currentPosition, currentTarget) < GameConstants.PATHWAYPOINTDISTANCE) {
            //Set next target
            setNextWayPoint(e.getId());
            if (!getWayPoint(path, e.getId(), currentTarget)) {
                //We reached the end - remove entity - do stuff
                ed.removeEntity(e.getId());
                return false;
            }
        }

        Vector2 currentVelocity = simplePhysics.getBody(e.getId()).getLinearVelocity();
        if (currentVelocity.x == 0 && currentVelocity.y == 0) {
            currentVelocity = new Vector2(0, 1);
        }
        //Move towards current target
        result.set(seek(currentTarget, currentPosition, currentVelocity));
        return true;
    }

    //Map the mobs to a path, to be used by other State (SteeringState?)
    private class MobDrivers extends EntityContainer<MobDriver> {

        public MobDrivers(EntityData ed) {
            super(ed, Drivable.class);
        }

        @Override
        protected MobDriver[] getArray() {
            return super.getArray();
        }

        @Override
        protected MobDriver addObject(Entity e) {
            SimpleBody body = simplePhysics.getBody(e.getId());
            MobDriver driver = new MobDriver();
            body.driver = driver;

            restartPath(e.getId());

            drivers.put(e, driver);

            return driver;
        }

        @Override
        protected void updateObject(MobDriver object, Entity e) {
            restartPath(e.getId());
        }

        @Override
        protected void removeObject(MobDriver object, Entity e) {
            drivers.remove(e);
        }

        public void updateDrivers() {
            this.update();

            for (Entity e : drivers.keySet()) {
                boolean createSteeringForce = createAndApplySteeringForce(e, tpf);
            }
        }
    }
}
