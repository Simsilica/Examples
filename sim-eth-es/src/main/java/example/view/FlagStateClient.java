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
import com.simsilica.es.EntitySet;
import example.ConnectionState;
import example.GameSessionState;
import example.es.Flag;
import example.es.Frequency;
import example.es.ViewType;

/**
 *
 * @author Asser
 */
public class FlagStateClient extends BaseAppState {

    private EntityData ed;
    private EntitySet flags;

    @Override
    protected void initialize(Application app) {

        this.ed = getState(ConnectionState.class).getEntityData();

        flags = ed.getEntities(Flag.class, Frequency.class, ViewType.class);
    }

    @Override
    protected void cleanup(Application app) {
        flags.release();
        flags = null;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        if (flags.applyChanges()) {

            for (Entity e : flags.getChangedEntities()) {
                Frequency freq = e.get(Frequency.class);
                int playerFrequency = getState(ShipFrequencyStateClient.class).getLocalPlayerFrequency();
                getState(ModelViewState.class).updateFlagModel(e, playerFrequency == freq.getFreq());
            }
            /*
            for (Entity e : flags.getAddedEntities()) {
                //Nothing to do here - all added flags are added as neutral. Changing flag freq happens by getChangedEntities

            }
            for (Entity e : flags.getRemovedEntities()) {
                //A flag must always have frequency, so if we end up here - there's nothing to do
            }
             */
        }
    }
}
