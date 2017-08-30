/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.view;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import example.ConnectionState;
import example.GameSessionState;
import example.es.Flag;
import example.es.Frequency;
import example.es.ShipType;

/**
 *
 * @author Asser
 */
public class ShipFrequencyStateClient extends BaseAppState {

    private EntityData ed;
    private EntitySet ships;
    private int localPlayerFrequency = -1; //Frequency initiated to -1. All real frequencies are positive integers

    @Override
    protected void initialize(Application app) {
        this.ed = getState(ConnectionState.class).getEntityData();
        ships = ed.getEntities(ShipType.class, Frequency.class);
    }

    @Override
    protected void cleanup(Application app) {
        ships.release();
        ships = null;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        if (ships.applyChanges()) {
            for (Entity e : ships.getChangedEntities()) {
                Frequency freq = e.get(Frequency.class);
                if (e.getId().getId() == getState(GameSessionState.class).getShipId().getId()) {
                    localPlayerFrequency = freq.getFreq();
                }
                //TODO: Use remaining information to update roster in HUD
            }
            for (Entity e : ships.getAddedEntities()) {
                Frequency freq = e.get(Frequency.class);
                if (e.getId().getId() == getState(GameSessionState.class).getShipId().getId()) {
                    localPlayerFrequency = freq.getFreq();
                }
                //TODO: Use information to update roster in HUD
            }
            for (Entity e : ships.getRemovedEntities()) {
                //TODO: Use information to update roster in HUD
            }
        }
    }

    public int getLocalPlayerFrequency() {
        return this.localPlayerFrequency;
    }
}
