package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntityData;
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
public class GateState extends AbstractGameSystem {

    private Ships shipContainer;
    private EntityData ed;
    
    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);
        
    }

    @Override
    protected void terminate() {
        
    }

    @Override
    public void update(SimTime tpf) {
        shipContainer.update();
    }

    @Override
    public void start() {
        shipContainer = new Ships(ed);
        shipContainer.start();
    }

    @Override
    public void stop() {
        shipContainer.stop();
        shipContainer = null;
    }

    public void requestShipChange(EntityId shipEntity, int shipType) {
        //TODO: Check for energy (full energy to switch ships)
        switch(shipType){
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

    /**
     * Keeps track of the count of the ships per team
     */
    private class Ships extends EntityContainer<FreqShipKey> {

        public Ships(EntityData ed) {
            super(ed, ShipType.class, Frequency.class);
        }
        
        //TODO: Not the best implementation, but this will rarely be called
        public int getCount(int freq, int ship){
            int count = 0;
            FreqShipKey comparator = new FreqShipKey(freq, ship);
            
            for(FreqShipKey fsKey : this.getArray()){
                if (fsKey.equals(comparator)) {
                    count++;
                }
            }
            
            return count;
        }
        
        //TODO: Not the best implementation, but this will rarely be called
        public int[] getCount(int freq){
            int[] count = new int[8];
            
            for(FreqShipKey fsKey : this.getArray()){
                if (fsKey.freq  == freq) {
                    count[fsKey.ship]++;
                }
            }
            
            return count;
        }

        @Override
        protected FreqShipKey addObject(Entity e) {
            ShipType st = e.get(ShipType.class);
            Frequency freq = e.get(Frequency.class);

            FreqShipKey fsKey = new FreqShipKey(freq.getFreq(), st.getType());
            
            return fsKey;
        }

        @Override
        protected FreqShipKey[] getArray() {
            return super.getArray();
        }

        @Override
        protected void updateObject(FreqShipKey object, Entity e) {
        }

        @Override
        protected void removeObject(FreqShipKey object, Entity e) {
        }
    }
    
    private class FreqShipKey{
        private final int freq;
        private final int ship;

        public FreqShipKey(int freq, int ship) {
            this.freq = freq;
            this.ship = ship;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + this.freq;
            hash = 43 * hash + this.ship;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FreqShipKey other = (FreqShipKey) obj;
            if (this.freq != other.freq) {
                return false;
            }
            if (this.ship != other.ship) {
                return false;
            }
            return true;
        }
        
        
    }
}
