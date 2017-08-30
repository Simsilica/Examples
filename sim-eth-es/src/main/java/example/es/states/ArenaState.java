package example.es.states;

import com.jme3.asset.AssetManager;
import com.jme3.system.JmeSystem;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.mathd.Vec3d;
import com.simsilica.mathd.Vec3i;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.es.ArenaId;
import example.es.Position;
import example.es.TileInfo;
import example.map.LevelFile;
import example.map.LevelLoader;
import example.sim.GameEntities;
import java.util.concurrent.ConcurrentHashMap;
import org.dyn4j.geometry.Convex;
import tiled.io.TMXMapReader;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import tiled.core.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State to keep track of different arenas. Arenas are composed of a tileset and
 * a ruleset and keeps track of where the next arena can be loaded
 *
 * @author Asser
 */
public class ArenaState extends AbstractGameSystem {

    private Map map;
    private EntityData ed;
    private EntitySet arenaEntities;
    private java.util.Map<Vector2, EntityId> index = new ConcurrentHashMap<>();
    private AssetManager am;
    static Logger log = LoggerFactory.getLogger(ArenaState.class);

    @Override
    protected void initialize() {

        this.ed = getSystem(EntityData.class);

        arenaEntities = ed.getEntities(ArenaId.class); //This filters all entities that are in arenas

        EntityId arenaId = GameEntities.createArena(0, ed); //Create first arena

        AssetManager am = JmeSystem.newAssetManager(Thread.currentThread().getContextClassLoader().getResource("com/jme3/asset/Desktop.cfg"));

        am.registerLoader(LevelLoader.class, "lvl");

    }

    public EntityId getEntityId(Vector2 coord) {
        return index.get(coord);
    }


    @Override
    protected void terminate() {
        //Release reader object
        // Release the entity set we grabbed previously
        arenaEntities.release();
        arenaEntities = null;
    }

    @Override
    public void update(SimTime tpf) {
        
        arenaEntities.applyChanges();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
