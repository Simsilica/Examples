/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es.states;

import com.simsilica.es.ComponentFilter;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.filter.FieldFilter;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.es.Bounty;
import example.es.Position;
import example.es.Spawner;
import example.es.SphereShape;
import example.sim.GameEntities;
import example.view.ModelViewState;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Asser
 */
public class BountyState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet bounties;

    private Map<EntityId, Integer> bountyCount = new HashMap<EntityId, Integer>();
    private EntitySet bountySpawners;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        bounties = ed.getEntities(Bounty.class);

        ComponentFilter bountySpawnerFilter = FieldFilter.create(Spawner.class, "type", Spawner.SpawnType.Bounties);

        bountySpawners = ed.getEntities(bountySpawnerFilter, Spawner.class, Position.class, SphereShape.class);
    }

    @Override
    protected void terminate() {
        bounties.release();
        bounties = null;

        bountySpawners.release();
        bountySpawners = null;
    }

    @Override
    public void update(SimTime time) {

        bounties.applyChanges();
        bountySpawners.applyChanges();

        for (Entity e : bountySpawners) { //Spawn max one per update-call / frame
            Spawner s = e.get(Spawner.class);
            Position p = e.get(Position.class);
            SphereShape c = e.get(SphereShape.class);
            if (bountyCount.containsKey(e.getId()) && bountyCount.get(e.getId()) < s.getMaxCount()) {
                spawnRandomBounty(p.getLocation(), c.getRadius());
                bountyCount.put(e.getId(), bountyCount.get(e.getId()) + 1);
            } else {
                spawnRandomBounty(p.getLocation(), c.getRadius());
                bountyCount.put(e.getId(), 1);
            }
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    private EntityId spawnRandomBounty(Vec3d spawnCenter, double radius) {

        double angle = Math.random() * Math.PI * 2;
        double x = Math.cos(angle) * radius + spawnCenter.x;
        double y = Math.sin(angle) * radius + spawnCenter.y;

        return GameEntities.createBounty(new Vec3d(x, y, 0), ed);
    }
}
