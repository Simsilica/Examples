package example.es.states;

import com.jme3.asset.AssetManager;
import com.jme3.system.JmeSystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.mathd.Vec3d;
import com.simsilica.mathd.Vec3i;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.es.GravityWell;
import example.es.WarpTo;
import example.map.LevelFile;
import example.map.LevelLoader;
import example.sim.GameEntities;
import java.util.concurrent.ConcurrentHashMap;
import tiled.io.TMXMapReader;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

/**
 * State
 *
 * @author Asser
 */
public class MapStateServer extends AbstractGameSystem {

    private TMXMapReader reader;
    private EntityData ed;
    private java.util.Map<Vector2, EntityId> index = new ConcurrentHashMap<>();
    private AssetManager am;
    public static final int MAP_SIZE = 1024;
    private static final int HALF = 512;

    @Override
    protected void initialize() {

        this.ed = getSystem(EntityData.class);

        am = JmeSystem.newAssetManager(Thread.currentThread().getContextClassLoader().getResource("com/jme3/asset/Desktop.cfg"));

        am.registerLoader(LevelLoader.class, "lvl");

        //
        //createEntitiesFromMap(loadMap("Maps/tunnelbase.lvl"), new Vec3d(0,0,0));
        //createEntitiesFromMap(loadMap("Maps/extreme.lvl"), new Vec3d(-MAP_SIZE,0,0));
        //createEntitiesFromMap(loadMap("Maps/tunnelbase.lvl"), new Vec3d(-MAP_SIZE, MAP_SIZE, 0));
        //createEntitiesFromMap(loadMap("Maps/turretwarz.lvl"), new Vec3d(0,MAP_SIZE,0));
    }

    public Vector2 getCenterOfArena(double currentXCoord, double currentYCoord) {
        double xArenaCoord = Math.floor(currentXCoord / MAP_SIZE);
        double yArenaCoord = Math.floor(currentYCoord / MAP_SIZE);

        double centerOfArenaX = currentXCoord < 0 ? xArenaCoord - HALF : xArenaCoord + HALF;
        double centerOfArenaY = currentYCoord < 0 ? yArenaCoord - HALF : yArenaCoord + HALF;

        return new Vector2(centerOfArenaX, centerOfArenaY);
    }

    public LevelFile loadMap(String mapFile) {
        LevelFile map = (LevelFile) am.loadAsset(mapFile);
        return map;
    }

    public void createEntitiesFromMap(LevelFile map, Vec3d arenaOffset) {
        short[][] tiles = map.getMap();

        for (int xpos = 0; xpos < tiles.length; xpos++) {
            for (int ypos = tiles[xpos].length - 1; ypos >= 0; ypos--) {
                short s = tiles[xpos][ypos];
                if (s != 0) {
                    //TODO: Check on the short and only create the map tiles, not the extras (asteroids, wormholes etc.)
/*  TILE    STATUS
                    Row 2, tile 1 - Border tile
                    Row 9, tile 10 - Vertical warpgate (Mostly open)
                    Row 9, tile 11 - Vertical warpgate (Frequently open)
                    Row 9, tile 12 - Vertical warpgate (Frequently closed)
                    Row 9, tile 13 - Vertical warpgate (Mostly closed)
                    Row 9, tile 14 - Horizontal warpgate (Mostly open)
                    Row 9, tile 15 - Horizontal warpgate (Frequently open)
                    Row 9, tile 16 - Horizontal warpgate (Frequently closed)
                    Row 9, tile 17 - Horizontal warpgate (Mostly closed)
     170    DONE    Row 9, tile 18 - Flag for turf
                    Row 9, tile 19 - Safezone
                    Row 10, tile 1 - Soccer goal (leave blank if you want)
                    Row 10, tile 2 - Flyover tile
                    Row 10, tile 3 - Flyover tile
                    Row 10, tile 4 - Flyover tile
                    Row 10, tile 5 - Flyunder (opaque) tile
                    Row 10, tile 6 - Flyunder (opaque) tile
                    Row 10, tile 7 - Flyunder (opaque) tile
                    Row 10, tile 8 - Flyunder (opaque) tile
                    Row 10, tile 9 - Flyunder (opaque) tile
                    Row 10, tile 10 - Flunder (opaque) tile
                    Row 10, tile 11 - Flyunder (opaque) tile
                    Row 10, tile 12 - Flyunder (opaque) tile
                    Row 10, tile 13 - Flyunder (black = transparent) tile
                    Row 10, tile 14 - Flyunder (black = transparent) tile
                    Row 10, tile 15 - Flyunder (black = transparent) tile
                    Row 10, tile 16 - Flyunder (black = transparent) tile
                    Row 10, tile 17 - Flyunder (black = transparent) tile
                    Row 10, tile 18 - Flyunder (black = transparent) tile
                    Row 10, tile 19 - Flyunder (black = transparent) tile
                     */
                    Vec3d location = new Vec3d(xpos, -ypos, 0).add(arenaOffset);
                    switch (s) {
                        case 170:
                            //Turf flag
                            GameEntities.createCaptureTheFlag(location, ed);
                            break;
                        case 216:
                            GameEntities.createOver1(location, ed);
                            break;
                        case 217:
                            //Medium asteroid
                            GameEntities.createOver2(location, ed);
                            break;
                        case 219:
                            //Station
                            break;
                        case 220:
                            GameEntities.createWormhole(location, 5, 5, 5000, GravityWell.PULL, new Vec3d(0, 0, 0), ed);
                            break;
                        default:
                            GameEntities.createMapTile(map.m_file, s, location, new Rectangle(1, 1), 0.0, ed);//
                            break;
                    }
                }
            }
        }
    }

    public EntityId getEntityId(Vector2 coord) {
        return index.get(coord);
    }

    protected boolean removeMapTileCoord(Vector2 vector2) {
        EntityId result = index.remove(vector2);
        if (result != null) {
            return true;
        }
        return false;
    }

    @Override
    protected void terminate() {
        //Release reader object
        reader = null;
    }

    @Override
    public void update(SimTime tpf) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public void editMap(double x, double y) {
        Vector2 coordinates = new Vector2(Math.round(x - 0.5) + 0.5, Math.round(y - 0.5) + 0.5);

        if (index.containsKey(coordinates)) {
            //TODO: There's already a tile there, attempt to change it or remove it

            //For now, just remove it
            ed.removeEntity(index.get(coordinates));

        } else {
            //GameEntities.createTileInfo(tileSet, 0, Vec3d.UNIT_X, c, y, ed)
            //GameEntities.createMapTile(MapTileTypes.solid(ed), new Vec3d(coordinates.x, coordinates.y, 0), ed, new Rectangle(1, 1)); //TODO: Account for actual arena (z-pos)
        }
    }
}
