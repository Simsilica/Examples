package example.es.states;

import com.jme3.network.HostedConnection;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.ethereal.EtherealHost;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.BodyPosition;
import example.es.HitPoints;
import example.es.PhysicsMassType;
import example.es.PhysicsShape;
import example.es.Position;
import example.es.WarpTo;
import example.es.WarpTouch;
import example.net.server.AccountHostedService;
import example.net.server.ZoneNetworkSystem;
import example.sim.GameEntities;
import example.sim.SimpleBody;
import example.sim.SimplePhysics;
import java.util.HashSet;
import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionListener;
import org.dyn4j.dynamics.contact.ContactConstraint;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Asser
 */
public class WarpState extends AbstractGameSystem implements CollisionListener {

    private SimTime time;

    private EntityData ed;
    private EntitySet warpTouchEntities, warpToEntities;
    private SimplePhysics simplePhysics;
    private HashSet<BodyFixture> warpTouchFixtures;
    private Warpers warpers;
    private EntitySet canWarp;

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        this.simplePhysics = getSystem(SimplePhysics.class);

        warpTouchEntities = ed.getEntities(WarpTouch.class);
        warpToEntities = ed.getEntities(BodyPosition.class, WarpTo.class);
        
        canWarp = ed.getEntities(BodyPosition.class, HitPoints.class);

    }

    @Override
    protected void terminate() {
        warpTouchEntities.release();
        warpTouchEntities = null;
    }

    @Override
    public void start() {
        simplePhysics.addCollisionListener(this);

        warpers = new Warpers(ed);
        warpers.start();
    }

    @Override
    public void stop() {
        simplePhysics.removeCollisionListener(this);

        warpers.stop();
        warpers = null;
    }

    @Override
    public void update(SimTime tpf) {
        time = tpf;

        //warpTouchEntities.applyChanges();
        warpers.update();
        
        canWarp.applyChanges();

        if (warpToEntities.applyChanges()) {
            for (Entity e : warpToEntities) {
                SimpleBody body = simplePhysics.getBody(e.getId());
                if (body != null) {
                    BodyPosition pos = e.get(BodyPosition.class);
                    Vec3d targetLocation = ed.getComponent(e.getId(), WarpTo.class).getTargetLocation();

                    Vector2 originalLocation = body.getTransform().getTranslation();
                    Vec3d origLocationVec3d = new Vec3d(originalLocation.x, originalLocation.y, 1);

                    //Right now, this is how I translate the ship (and everything else) to the new location
                    body.getTransform().setTranslation(targetLocation.x, targetLocation.y);

                    //This is how it 'could also be done?'
                    /*
                    HostedConnection hc = getSystem(AccountHostedService.class).getHostedConnection(e.getId());
                    if (hc != null) {
                        getSystem(EtherealHost.class).setConnectionObject(getSystem(AccountHostedService.class).getHostedConnection(e.getId()), e.getId().getId(), targetLocation);
                    }
                     */
                    //getStateListener(hc).setSelf(selfId, initialPosition);
                    GameEntities.createWarpEffect(origLocationVec3d, ed);
                    GameEntities.createWarpEffect(targetLocation, ed);

                    ed.removeComponent(e.getId(), WarpTo.class);
                }
                else{
                    throw new RuntimeException("Entity has a body position, but no physical body");
                }
            }
        }
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2) {
        return true;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {
        return true;
    }

    //Contact manifold created by the manifold solver
    @Override
    public boolean collision(org.dyn4j.dynamics.Body body1, BodyFixture fixture1, org.dyn4j.dynamics.Body body2, BodyFixture fixture2, Manifold manifold) {
        EntityId one = (EntityId) body1.getUserData();
        EntityId two = (EntityId) body2.getUserData();

        if ((warpers.getObject(one) != null && warpers.getObject(one).getWarpFixture() == fixture1) || (warpers.getObject(two) != null && warpers.getObject(two).getWarpFixture() == fixture2)) {
            if (warpers.getObject(one) != null && warpers.getObject(one).getWarpFixture() == fixture1) {
                Vector2 targetLocation = warpers.getObject(one).getTargetLocation();
                WarpTo warpTo = new WarpTo(new Vec3d(targetLocation.x, targetLocation.y, 1));
                ed.setComponent(two, warpTo);
            }

            if (warpers.getObject(two) != null && warpers.getObject(two).getWarpFixture() == fixture2) {
                Vector2 targetLocation = warpers.getObject(two).getTargetLocation();
                WarpTo warpTo = new WarpTo(new Vec3d(targetLocation.x, targetLocation.y, 1));
                ed.setComponent(one, warpTo);
            }
            return false;
        }

        return true;
    }

    //Contact constraint created
    @Override
    public boolean collision(ContactConstraint contactConstraint) {
        return true;
    }

    private class WarpFixture {

        private Vector2 targetLocation;
        private BodyFixture warpFixture;

        public WarpFixture(Vector2 targetLocation, BodyFixture warpFixture) {
            this.targetLocation = targetLocation;
            this.warpFixture = warpFixture;
        }

        public Vector2 getTargetLocation() {
            return targetLocation;
        }

        public void setTargetLocation(Vector2 targetLocation) {
            this.targetLocation = targetLocation;
        }

        public BodyFixture getWarpFixture() {
            return warpFixture;
        }

        public void setWarpFixture(BodyFixture warpFixture) {
            this.warpFixture = warpFixture;
        }
    }
    
    public void requestWarpToCenter(EntityId eID)
    {
        Transform t = simplePhysics.getBody(eID).getTransform();
                
        Vector2 centerOfArena = getSystem(MapStateServer.class).getCenterOfArena(t.getTranslationX(), t.getTranslationY());
        
        Vec3d res = new Vec3d(centerOfArena.x, centerOfArena.y, 0);
        
        ed.setComponent(eID, new WarpTo(res));
    }

    //Entities that upon touched will warp the other body away
    private class Warpers extends EntityContainer<WarpFixture> {

        public Warpers(EntityData ed) {
            super(ed, WarpTouch.class, Position.class, PhysicsMassType.class, PhysicsShape.class);
        }

        @Override
        protected WarpFixture[] getArray() {
            return super.getArray();
        }

        @Override
        protected WarpFixture addObject(Entity e) {
            WarpTouch wt = e.get(WarpTouch.class);

            Body b = simplePhysics.getBody(e.getId());
            BodyFixture bf = b.getFixture(0);

            Vector2 targetLocation = new Vector2(wt.getTargetLocation().x, wt.getTargetLocation().y);

            return new WarpFixture(targetLocation, bf);
        }

        @Override
        protected void updateObject(WarpFixture object, Entity e) {
            //Do not support live-updating warpers
        }

        @Override
        protected void removeObject(WarpFixture object, Entity e) {

        }
    }
}
