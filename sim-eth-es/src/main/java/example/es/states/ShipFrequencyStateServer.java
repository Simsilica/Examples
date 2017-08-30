package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.Frequency;
import example.es.ShipType;
import example.es.ShipTypes;
import example.es.ViewTypes;

/**
 *
 * @author Asser
 */
public class ShipFrequencyStateServer extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet freqs;

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        this.freqs = ed.getEntities(Frequency.class, ShipType.class);
    }

    @Override
    protected void terminate() {
        freqs.release();
        freqs = null;
    }

    @Override
    public void update(SimTime tpf) {
        if (freqs.applyChanges()) {
            for(Entity e : freqs.getAddedEntities()){
                
            }
            for(Entity e : freqs.getChangedEntities()){
                
            }
            for(Entity e : freqs.getRemovedEntities()){
                
            }
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public void requestShipChange(EntityId shipEntity, int shipType) {
        //TODO: Check for energy (full energy to switch ships)
        switch (shipType) {
            case 1:
                ed.setComponent(shipEntity, ShipTypes.warbird(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_warbird(ed));
                break;
            case 2:
                ed.setComponent(shipEntity, ShipTypes.javelin(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_javelin(ed));
                break;
            case 3:
                ed.setComponent(shipEntity, ShipTypes.spider(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_spider(ed));
                break;
            case 4:
                ed.setComponent(shipEntity, ShipTypes.leviathan(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_levi(ed));
                break;
            case 5:
                ed.setComponent(shipEntity, ShipTypes.terrier(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_terrier(ed));
                break;
            case 6:
                ed.setComponent(shipEntity, ShipTypes.weasel(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_weasel(ed));
                break;
            case 7:
                ed.setComponent(shipEntity, ShipTypes.lancaster(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_lanc(ed));
                break;
            case 8:
                ed.setComponent(shipEntity, ShipTypes.shark(ed));
                ed.setComponent(shipEntity, ViewTypes.ship_shark(ed));
                break;
        }
    }

    public int getFrequency(EntityId entityId) {
        Frequency freq = ed.getComponent(entityId, Frequency.class);

        return freq.getFreq();
    }

    public void requestFreqChange(EntityId eId, int newFreq) {

    }
}
