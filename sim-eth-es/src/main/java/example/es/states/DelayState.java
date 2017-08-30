/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.Delay;
import java.util.Iterator;

/**
 *
 * @author Asser
 */
public class DelayState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;

    @Override
    public void update(SimTime tpf) {
        entities.applyChanges();
        for (Entity e : entities) {
            Delay d = e.get(Delay.class);
            if (d.getPercent() >= 1.0) {
                Iterator<EntityComponent> componentIterator = d.getDelayedComponents().iterator();
                switch (d.getType()) {
                    case Delay.REMOVE:
                        while (componentIterator.hasNext()) {
                            ed.removeComponent(e.getId(), componentIterator.next().getClass());
                        }
                        break;
                    case Delay.SET:

                        while (componentIterator.hasNext()) {
                            ed.setComponent(e.getId(), componentIterator.next());
                        }
                        break;
                }

                ed.removeComponent(e.getId(), Delay.class);
            }
        }
    }

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        entities = ed.getEntities(Delay.class); //This filters all entities that have delayed components
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }
}
