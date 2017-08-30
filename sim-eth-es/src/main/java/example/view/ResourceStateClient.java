/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.view;

import com.jme3.app.Application;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.state.DebugHudState;
import example.ConnectionState;
import example.es.Gold;
import example.net.client.GameSessionClientService;

/**
 *
 * @author Asser
 */
public class ResourceStateClient extends BaseAppState {

    private VersionedHolder<String> goldDisplay;
    private EntitySet resources;
    private EntityId shipId;

    @Override
    protected void initialize(Application aplctn) {
        if (getState(DebugHudState.class) != null) {
            DebugHudState debug = getState(DebugHudState.class);
            this.goldDisplay = debug.createDebugValue("Gold", DebugHudState.Location.Top);

        }

        this.resources = getState(ConnectionState.class).getEntityData().getEntities(Gold.class);

        shipId = getState(ConnectionState.class).getService(GameSessionClientService.class).getShip();
    }

    @Override
    protected void cleanup(Application aplctn) {
        resources.release();
        resources = null;
    }

    @Override
    protected void enable() {

    }

    @Override
    protected void disable() {

    }

    @Override
    public void update(float tpf) {
        // Display Gold
        if (resources.applyChanges()) {
            Entity e = resources.getEntity(shipId);
            Gold g = e.get(Gold.class);
            goldDisplay.setObject(String.valueOf(g.getGold()));
        }
    }

}
