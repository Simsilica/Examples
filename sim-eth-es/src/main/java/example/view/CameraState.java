package example.view;

import com.jme3.app.Application;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.BaseAppState;

/**
 * A state to manage in-game camera 
 * @author Asser
 */
public class CameraState extends BaseAppState{

    private static final float DISTANCETOPLANE = 60;
    private Camera camera;

    public Camera getCamera() {
        return camera;
    }
    private ModelViewState models;
    private Spatial playerShip;
    

    @Override
    protected void initialize(Application app) {
        this.camera = app.getCamera();
        this.models = getState(ModelViewState.class);
        this.playerShip = models.getPlayerSpatial();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }
    
    @Override
    public void update(float tpf) {
        if( playerShip != null ) {
            camera.setLocation(playerShip.getWorldTranslation().add(0,0,DISTANCETOPLANE));  //Set camera position above spatial - Z is up
            camera.lookAt(playerShip.getWorldTranslation(), Vector3f.UNIT_Z); //Set camera to look at the spatial
        }
        else //Probably a crude way to do it - should be handled properly
        {
            this.playerShip = models.getPlayerSpatial(); 
        }
    }
    
    public void setPlayerShip(Spatial s){
        this.playerShip = s;
    }
}
