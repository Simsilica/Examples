package example.es.states;

import com.dongbat.walkable.FloatArray;
import com.dongbat.walkable.PathHelper;
import com.jme3.math.Vector2f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.es.BaseType;
import example.es.BodyPosition;
import example.es.MobPath;
import example.es.MobType;
import example.es.PhysicsMassType;
import example.es.PhysicsShape;
import example.es.Position;
import example.es.SteeringPath;
import example.es.TowerType;
import example.sim.SimplePhysics;
import hxDaedalus.data.Obstacle;
import java.util.HashMap;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Asser
 */
public class PathfinderState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet mobs;
    private MobPaths mobPaths;
    private BasePositions basePositions;
    private PathHelper pathHelper;
    private TowerObstacles towerObstacles;
    private final HashMap<EntityId, Vector2f> mobTargetMap = new HashMap<>();
    private long time;
    private SimplePhysics simplePhysics;
    private Vector2f pathHelperOffset;
    static Logger log = LoggerFactory.getLogger(PathfinderState.class);

    @Override
    protected void initialize() {

        this.ed = getSystem(EntityData.class);

        this.mobs = ed.getEntities(BodyPosition.class, PhysicsShape.class, MobType.class);

        //TODO: Figure out an appropriate width and height
        float width = GameConstants.PATHHELPERWIDTH;
        float height = GameConstants.PATHHELPERHEIGHT;
        pathHelper = new PathHelper(width, height);

        simplePhysics = getSystem(SimplePhysics.class);

        pathHelperOffset = new Vector2f(GameConstants.PATHHELPERWIDTH / 2, GameConstants.PATHHELPERHEIGHT / 2);
    }

    @Override
    protected void terminate() {
        this.mobs.release();
        this.mobs = null;
    }

    @Override
    public void update(SimTime tpf) {
        time = tpf.getTime();
        towerObstacles.update();
        mobPaths.update();
        basePositions.update();

        mobs.applyChanges();
    }

    @Override
    public void start() {
        mobPaths = new MobPaths(ed);
        mobPaths.start();

        towerObstacles = new TowerObstacles(ed);
        towerObstacles.start();

        basePositions = new BasePositions(ed);
        basePositions.start();
    }

    @Override
    public void stop() {
        mobs.clear();
        
        mobPaths.stop();
        mobPaths = null;

        towerObstacles.stop();
        towerObstacles = null;

        basePositions.stop();
        basePositions = null;
    }

    //For all mobs, returns true if everyone could find a path
    private boolean recalculatePaths() {
        boolean aPathForEveryone = true;

        for (Entity e : mobs) {
            FloatArray path = new FloatArray();
            //TODO: Instead of a random base, pick the nearest
            aPathForEveryone = aPathForEveryone && calculateAndCreatePath(e, getRandomBasePosition(), path);
            mobPaths.updateObject(path, e);
        }
        return aPathForEveryone;
    }

    //For a single mob, returns true if path found
    private boolean calculateAndCreatePath(Entity e, Vector2f endPosition, FloatArray path) {

        //Get position
        Vector2 startVec2 = simplePhysics.getBody(e.getId()).getTransform().getTranslation();
        Vector2f startPosition = new Vector2f((float) startVec2.x, (float) startVec2.y);
        //Offset start
        startPosition = startPosition.add(pathHelperOffset);
        //Offset end
        endPosition = endPosition.add(pathHelperOffset);

        //Get shape
        PhysicsShape physicsShape = e.get(PhysicsShape.class);

        //Mob radius
        float mobRadius = (float) physicsShape.getFixture().getShape().getRadius();

        //Find path
        pathHelper.findPath((float) startPosition.x, (float) startPosition.y,
                (float) endPosition.x, (float) endPosition.y,
                mobRadius, path);

        //Offset path so it fits with the physical world (since jWalkable only works in width/heieght not in coordinates)
        for (int i = 0; i < path.size; i = i + 2) {
            path.set(i, path.get(i) - pathHelperOffset.x);
        }
        for (int i = 1; i < path.size; i = i + 2) {
            path.set(i, path.get(i) - pathHelperOffset.y);
        }

        mobTargetMap.put(e.getId(), endPosition);

        if (path.size > 0) {
            log.info(path.toString());
            ed.setComponent(e.getId(), new MobPath(path));
            return true;
        }

        return false;
    }

    private Vector2f getRandomBasePosition() {
        //Find random base
        Vector2f[] basePosArray = basePositions.getArray();
        int randIndex = (int) (Math.random() * basePosArray.length);
        //Vector2f randomEndPosition = basePositions.getArray()[randIndex];
        Vector2f randomEndPosition = new Vector2f(10, 5);
        return randomEndPosition;
    }

    //Map the mobs to a path, to be used by other State (SteeringState?)
    private class MobPaths extends EntityContainer<FloatArray> {

        public MobPaths(EntityData ed) {
            super(ed, Position.class, MobType.class, PhysicsShape.class, PhysicsMassType.class, SteeringPath.class);
        }

        @Override
        protected FloatArray[] getArray() {
            return super.getArray();
        }

        @Override
        protected FloatArray addObject(Entity e) {

            FloatArray path = new FloatArray();
            //Give this mob direction
            calculateAndCreatePath(e, getRandomBasePosition(), path);
            
            return path;
        }

        @Override
        protected void updateObject(FloatArray object, Entity e) {

        }

        @Override
        protected void removeObject(FloatArray object, Entity e) {
            mobTargetMap.remove(e.getId());
        }
    }

    //Map the towers to a polygon shape
    private class TowerObstacles extends EntityContainer<Obstacle> {

        public TowerObstacles(EntityData ed) {
            super(ed, Position.class, TowerType.class, PhysicsShape.class);
        }

        @Override
        protected Obstacle[] getArray() {
            return super.getArray();
        }

        @Override
        protected Obstacle addObject(Entity e) {

            //Add the towers shape to the pathhelper as obstacle
            Position position = e.get(Position.class);
            PhysicsShape physicsShape = e.get(PhysicsShape.class);

            //Convert Dyn4j shape to jWalkable polygon/vertices
            Polygon p = (Polygon) physicsShape.getFixture().getShape();
            Vector2[] vertices = p.getVertices();
            float[] verts = new float[vertices.length*2];

            int i = 0;
            for(Vector2 vec : vertices){
                
                verts[i++] = (float) vec.x;
                verts[i++] = (float) vec.y;
            }
            
            
            //TODO: Offset position to jWalkable width+height

            float x = (float) position.getLocation().x + pathHelperOffset.x;
            float y = (float) position.getLocation().y + pathHelperOffset.y;

            //Add obstacle to mesh
            Obstacle o = pathHelper.addPolygon(verts, x, y);
            //Recalculate paths
            recalculatePaths();

            return o;
        }

        @Override
        protected void updateObject(Obstacle object, Entity e) {
            //TODO: If a tower changes size, we should change the obstacle size and recalculate paths
        }

        @Override
        protected void removeObject(Obstacle object, Entity e) {
            //Remove obstacle on mesh
            pathHelper.removeObstacle(object);
            //Recalculate paths
            recalculatePaths();
        }
    }

    //Map the bases to a position
    private class BasePositions extends EntityContainer<Vector2f> {

        public BasePositions(EntityData ed) {
            super(ed, Position.class, BaseType.class);
        }

        @Override
        protected Vector2f[] getArray() {
            return super.getArray();
        }

        @Override
        protected Vector2f addObject(Entity e) {
            Position position = e.get(Position.class);

            //Recalculate paths since we added a base
            recalculatePaths();

            return new Vector2f((float) position.getLocation().x, (float) position.getLocation().y);
        }

        @Override
        protected void updateObject(Vector2f object, Entity e) {
            //TODO: If a base moves, we should move the obstacle and recalculate paths
        }

        @Override
        protected void removeObject(Vector2f object, Entity e) {
            //TODO: If we remove a base, only recalculate the paths for mobs going to this exact base

            //Recalculate paths since we removed a base
            recalculatePaths();
        }
    }
}
