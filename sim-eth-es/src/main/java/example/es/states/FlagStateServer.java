package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.Flag;
import example.es.Frequency;
import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.dynamics.BodyFixture;

/**
 *
 * @author Asser
 */
public class FlagStateServer extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet teamFlags;
    private ShipFrequencyStateServer shipState;

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        teamFlags = ed.getEntities(Flag.class, Frequency.class);

        shipState = getSystem(ShipFrequencyStateServer.class);
    }

    @Override
    protected void terminate() {
        teamFlags.release();
        teamFlags = null;

    }

    public boolean isFlag(EntityId flagEntityId) {
        return teamFlags.getEntityIds().contains(flagEntityId);
    }

    @Override
    public void update(SimTime tpf) {
        teamFlags.applyChanges();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public boolean collide(org.dyn4j.dynamics.Body body1, BodyFixture fixture1, org.dyn4j.dynamics.Body body2, BodyFixture fixture2, Manifold manifold, double tpf) {
        EntityId one = (EntityId) body1.getUserData();
        EntityId two = (EntityId) body2.getUserData();

        if (isFlag(one)) {
            int flagFreq = ed.getComponent(one, Frequency.class).getFreq();
            int shipFreq = shipState.getFrequency(two);

            if (shipFreq != flagFreq) {
                ed.setComponent(one, new Frequency(shipFreq));
            }

        } else if (isFlag(two)) {
            int flagFreq = ed.getComponent(two, Frequency.class).getFreq();
            int shipFreq = shipState.getFrequency(one);

            if (shipFreq != flagFreq) {
                ed.setComponent(two, new Frequency(shipFreq));
            }
        } else {
            return true;
        }
        return false;
    }
}
