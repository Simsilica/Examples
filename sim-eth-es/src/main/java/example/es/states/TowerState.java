package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.Gold;
import example.es.Position;
import example.es.ShipType;
import example.es.TowerType;
import example.sim.GameEntities;
import example.sim.PhysicsShapes;
import example.sim.SimplePhysics;
import org.dyn4j.collision.Filter;
import org.dyn4j.geometry.Convex;

/**
 * State
 *
 * @author Asser
 */
public class TowerState extends AbstractGameSystem {

    private EntityData ed;
    private SimplePhysics simplePhysics;
    private EntitySet towers;
    private ResourceState resourceState;

    @Override
    protected void initialize() {

        this.ed = getSystem(EntityData.class);
        this.simplePhysics = getSystem(SimplePhysics.class);
        
        this.resourceState = getSystem(ResourceState.class);
        
        this.towers = ed.getEntities(TowerType.class, Position.class);
    }

    @Override
    protected void terminate() {
        //Release reader object
        towers.release();
        towers = null;
    }

    @Override
    public void update(SimTime tpf) {

        if (towers.applyChanges()) {
            for (Entity e : towers.getAddedEntities()) {

            }

            for (Entity e : towers.getChangedEntities()) {

            }

            for (Entity e : towers.getRemovedEntities()) {

            }
        }
        
        
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public void editTower(double x, double y, EntityId owner) {
        Convex c = PhysicsShapes.tower().getFixture().getShape();
        c.translate(x, y);
        //Can we build there and do we have the money?
        if (simplePhysics.allowConvex(c) && resourceState.canAffordTower(owner)) {
            //Create tower
            GameEntities.createTower(new Vec3d(x, y, 0), ed);
            //Deduct cost
            resourceState.buyTower(owner);
        }
    }
}
