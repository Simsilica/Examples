/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.es.Gold;
import example.es.ShipType;
import java.util.HashMap;

/**
 *
 * @author ss
 */
public class ResourceState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet ships;
    private double time_since_last_update;
    private HashMap<EntityId, Integer> goldMap = new HashMap<>();

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);
        this.ships = this.ed.getEntities(ShipType.class);
    }

    @Override
    protected void terminate() {
        this.ships.release();
        this.ships = null;
    }

    @Override
    public void update(SimTime tpf) {
        // only update every RESOURCE_UPDATE_INTERVAL
        this.ships.applyChanges();
        
        if (this.time_since_last_update > GameConstants.RESOURCE_UPDATE_INTERVAL) {
            this.time_since_last_update = 0;

            //TPF is in seconds
            int gold = (int) (tpf.getTpf() * GameConstants.GOLD_PER_SECOND);

            //Handle old ships
            for (Entity e : this.ships) {
                Gold g = this.ed.getComponent(e.getId(), Gold.class);
                int totalGold = g.getGold() + gold;
                this.ed.setComponent(e.getId(), new Gold(totalGold));

                goldMap.put(e.getId(), totalGold);
            }
        }
        // update time
        this.time_since_last_update += tpf.getTpf();

    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public boolean canAffordTower(EntityId owner) {
        return goldMap.get(owner) >= GameConstants.TOWERCOST;
    }

    public void buyTower(EntityId owner) {
        int currentGold = goldMap.get(owner);
        int newGold = currentGold - GameConstants.TOWERCOST;
        ed.setComponent(owner, new Gold(newGold));
        goldMap.put(owner, newGold);
    }
}
