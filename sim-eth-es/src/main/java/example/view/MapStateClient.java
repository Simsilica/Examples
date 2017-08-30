package example.view;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.texture.Image;
import com.jme3.texture.plugins.AWTLoader;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import example.ConnectionState;
import example.es.Position;
import example.es.TileInfo;
import example.map.LevelFile;
import example.map.LevelLoader;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import tiled.io.TMXMapReader;
import org.dyn4j.geometry.Vector2;
import tiled.core.Map;

/**
 * State
 *
 * @author Asser
 */
public class MapStateClient extends BaseAppState {

    private TMXMapReader reader;
    private Map map;
    private EntityData ed;
    private BodyContainer tileImages;
    private java.util.Map<Vector2, EntityId> index = new ConcurrentHashMap<>();
    private AssetManager am;
    private HashMap<String, LevelFile> levelFiles = new HashMap<>();
    private AWTLoader imgLoader;
    private HashMap<TileKey, Image> imageMap = new HashMap<>();

    @Override
    protected void initialize(Application app) {

        this.ed = getState(ConnectionState.class).getEntityData();

        this.am = app.getAssetManager();

        am.registerLoader(LevelLoader.class, "lvl");

        imgLoader = new AWTLoader();

    }

    public Image getImage(EntityId entityId) {
        return tileImages.getObject(entityId);
    }

    protected LevelFile loadMap(String tileSet) {

        LevelFile map = (LevelFile) am.loadAsset(tileSet);

        return map;
    }

    public EntityId getEntityId(Vector2 coord) {
        return index.get(coord);
    }

    @Override
    protected void cleanup(Application app) {
        //Release reader object
        reader = null;
    }

    @Override
    protected void onEnable() {

        tileImages = new MapStateClient.BodyContainer(ed);
        tileImages.start();
    }

    @Override
    protected void onDisable() {
        tileImages.stop();
        tileImages = null;
    }

    @Override
    public void update(float tpf) {
        tileImages.update();
    }

    //Map the entities to their texture
    private class BodyContainer extends EntityContainer<Image> {

        /**
         * Converts a given Image into a BufferedImage
         *
         * @param img The Image to be converted
         * @return The converted BufferedImage
         */
        private BufferedImage toBufferedImage(java.awt.Image img) {
            if (img instanceof BufferedImage) {
                return (BufferedImage) img;
            }

            int width = img.getWidth(null);
            int height = img.getHeight(null);

            // Create a buffered image with transparency
            BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Draw the image on to the buffered image
            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(img, 0, 0, null);                          //No flip
            //bGr.drawImage(img, 0 + width, 0, -width, height, null);  //Horisontal flip
            //bGr.drawImage(img, 0, 0 + height, width, -height, null);   //Vertical flip
            bGr.dispose();

            // Return the buffered image
            return bimage;
        }

        public BodyContainer(EntityData ed) {
            super(ed, Position.class, TileInfo.class);
        }

        @Override
        protected Image[] getArray() {
            return super.getArray();
        }

        @Override
        protected Image addObject(Entity e) {

            TileInfo ti = e.get(TileInfo.class);
            String tileSet = ti.getTileSet();
            short tileIndex = ti.getTileIndex();
            TileKey key = new TileKey(tileSet, tileIndex);

            if (!imageMap.containsKey(key)) {

                if (!levelFiles.containsKey(tileSet)) {
                    LevelFile lf = loadMap(tileSet); //TODO: Should be done in a non-intrusive way
                    levelFiles.put(tileSet, lf);

                }

                java.awt.Image awtInputImage = levelFiles.get(tileSet).getTiles()[tileIndex - 1];
                Image jmeOutputImage = imgLoader.load(this.toBufferedImage(awtInputImage), true);

                imageMap.put(key, jmeOutputImage);

            }

            return imageMap.get(key);
        }

        @Override
        protected void updateObject(Image object, Entity e) {
            //Does not support mass updating tiles right now
            //TODO: The tileIndex in a TileInfo component could change
        }

        @Override
        protected void removeObject(Image object, Entity e) {
            //We leave the levels loaded
        }
    }

    public class TileKey {

        private final String tileSet;
        private final short tileIndex;

        public TileKey(String x, short y) {
            this.tileSet = x;
            this.tileIndex = y;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.tileSet);
            hash = 97 * hash + this.tileIndex;
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
            final TileKey other = (TileKey) obj;
            if (this.tileIndex != other.tileIndex) {
                return false;
            }
            if (!Objects.equals(this.tileSet, other.tileSet)) {
                return false;
            }
            return true;
        }

    }
}
