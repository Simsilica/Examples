/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package example.view;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.*;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.*;
import com.jme3.texture.Texture;
import com.jme3.util.SafeArrayList;

import com.simsilica.lemur.*;
import com.simsilica.lemur.style.ElementId;

import com.simsilica.ethereal.EtherealClient;
import com.simsilica.ethereal.SharedObject;
import com.simsilica.ethereal.SharedObjectListener;

import com.simsilica.mathd.trans.PositionTransition3f;
import com.simsilica.mathd.trans.TransitionBuffer;

import example.ConnectionState;
import example.GameSessionState;
import example.Main;
import example.TimeState;
import example.net.GameSessionListener;
import example.net.client.GameSessionClientService;

/**
 *  Displays the models for the various physics objects.
 *
 *  @author    Paul Speed
 */
public class ModelViewState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(ModelViewState.class);

    private Node modelRoot;
    private Node hudLabelRoot;
    private Camera camera;
    
    private GameSessionObserver gameSessionObserver = new GameSessionObserver();
    private Map<Integer, ObjectInfo> index = new ConcurrentHashMap<>();
    private Map<Integer, PlayerInfo> playerIndex = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<ObjectInfo> toAdd = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Integer> toRemove = new ConcurrentLinkedQueue<>();   

    private Map<Integer, Spatial> models = new HashMap<>();
    private SafeArrayList<ObjectInfo> objects = new SafeArrayList<>(ObjectInfo.class); 

    private TimeState timeState;     
    private SharedObjectUpdater objectUpdater = new SharedObjectUpdater();


    public ModelViewState() {
    }

    public Spatial getModel( Integer id ) {
        ObjectInfo info = index.get(id);
        if( info == null ) {
            return null;
        }
        return info.spatial;
    }

    @Override
    protected void initialize( Application app ) {
        modelRoot = new Node();
        hudLabelRoot = new Node("HUD labels");
        
        this.camera = app.getCamera();
 
        // Add a listener to receive efficient object updates.   
        getState(ConnectionState.class).getService(EtherealClient.class).addObjectListener(objectUpdater);
        
        // Retrieve the time source from the network connection
        // The time source will give us a time in recent history that we should be
        // viewing.  This currently defaults to -100 ms but could vary (someday) depending
        // on network connectivity.
        // For more information on this interpolation approach, see the Valve networking
        // articles at:
        // https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking
        // https://developer.valvesoftware.com/wiki/Latency_Compensating_Methods_in_Client/Server_In-game_Protocol_Design_and_Optimization
        //this.timeSource = getState(ConnectionState.class).getRemoteTimeSource();
        // 
        // We now grab time from the TimeState which wraps the TimeSource to give
        // consistent timings over the whole frame
        timeState = getState(TimeState.class);
        
        // Still need this listener because it's the only way we know things
        // like player name which we might use later.
        getState(ConnectionState.class).getService(GameSessionClientService.class).addGameSessionListener(gameSessionObserver);
                        
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        ((Main)getApplication()).getRootNode().attachChild(modelRoot);
        ((Main)getApplication()).getGuiNode().attachChild(hudLabelRoot);
    }

    @Override
    protected void onDisable() {
        modelRoot.removeFromParent();
        hudLabelRoot.removeFromParent();
    }
    
    protected void addModel( ObjectInfo info ) {
 
        // Look up the name for the player
        PlayerInfo playerInfo = playerIndex.get(info.shipId);
        if( playerInfo != null ) {
            info.setPlayerName(playerInfo.playerName);
        }
 
        Spatial model = createShip(-1, "", info.shipId); 
        models.put(info.shipId, model);       
        modelRoot.attachChild(model);
        
        info.spatial = model;
        objects.add(info);
        
        // If the ship is our ship then we'll hide it else it looks bad.
        if( info.shipId == getState(GameSessionState.class).getShipId() ) {
            info.localPlayerShip = true;
        }
        
        // Set the cull hint based on the visibility setting
        info.resetVisibility();
    }
    
    protected void removeModel( int id ) {
        log.info("removeModel(" + id + ")");
        ObjectInfo info = index.remove(id);
        if( info != null ) {
            // Remove it from our iteration list
            objects.remove(info);
            info.dispose();
        }
        Spatial spatial = models.remove(id);
        if( spatial != null ) {
            spatial.removeFromParent();
        }
    }

    @Override
    public void update( float tpf ) {
        if( !toAdd.isEmpty() ) {
            ObjectInfo info;
            while( (info = toAdd.poll()) != null ) {
                addModel(info);
            }
        }
        if( !toRemove.isEmpty() ) {
            Integer id;
            while( (id = toRemove.poll()) != null ) {
                removeModel(id);
            }
        }
 
        // Grab a consistent time for this frame
        long time = timeState.getTime();

        // Update all of the models
        for( ObjectInfo info : objects.getArray() ) {
            info.updateSpatial(time);
        }
    }

    protected Spatial createShip( int clientId, String playerName, int shipId ) {
    
        AssetManager assetManager = getApplication().getAssetManager();
        
        Spatial ship = assetManager.loadModel("Models/fighter.j3o");
        ship.center();
        Texture texture = assetManager.loadTexture("Textures/ship1.png");
        Material mat = GuiGlobals.getInstance().createMaterial(texture, false).getMaterial();
        ship.setMaterial(mat);
 
        Node result = new Node("ship:" + shipId);
        result.attachChild(ship);        
 
        result.setUserData("clientId", clientId);
        result.setUserData("playerName", playerName);
        result.setUserData("objectId", shipId);
        
        return result;
    }

    protected ObjectInfo getObjectInfo( int shipId, boolean create ) {
        ObjectInfo result = index.get(shipId);
        if( result == null && create ) {
            result = new ObjectInfo(shipId);
            index.put(shipId, result);
            toAdd.add(result);
        }
        return result;
    }
 
    private class PlayerInfo {
        int clientId;
        String playerName;
        int shipId;
        
        public PlayerInfo( int clientId, String playerName, int shipId ) {
            this.clientId = clientId;
            this.playerName = playerName;
            this.shipId = shipId;
        }
    }
     
    private class ObjectInfo {
        int shipId;
        Spatial spatial;
        boolean visible;
        boolean localPlayerShip;
 
        // Keep a HUD label.  Why not?  Normally I'd do this in a 
        // separate state/system but this isn't an ES so let's just
        // grow the spaghetti!
        Label shipLabel;
        float labelOffset = 0.1f;
        
        volatile Vector3f updatePos;
        volatile Quaternion updateRot;
        
        TransitionBuffer<PositionTransition3f> buffer;
        
        public ObjectInfo( int shipId ) {
            this.shipId = shipId;
            // A history of 12 should give us about 200 ms of history
            // through which to interpolate.  12 * 1/60 = 12/60 = 1/5 = 200 ms.
            this.buffer = new TransitionBuffer<>(12);
            
            this.shipLabel = new Label("Ship", new ElementId("ship.label"));
            shipLabel.setColor(ColorRGBA.Green);
            shipLabel.setShadowColor(ColorRGBA.Black);
            
            // The first valid history entry will turn us visible
            setVisible(false);
        }
 
        public void setPlayerName( String playerName ) {
            shipLabel.setText(playerName);
        }
 
        protected void setVisible( boolean f ) {
            if( this.visible == f ) {
                return;
            }
            this.visible = f;
            resetVisibility();
        }
 
        protected void resetVisibility() {
            if( spatial == null ) {
                return;
            }
            if( visible && !localPlayerShip ) {
                spatial.setCullHint(Spatial.CullHint.Inherit);
                shipLabel.setCullHint(Spatial.CullHint.Inherit);
            } else {
                spatial.setCullHint(Spatial.CullHint.Always);
                shipLabel.setCullHint(Spatial.CullHint.Always);
            }
        }
        
        protected void updateLabelPos( Vector3f shipPos ) {
            if( !visible || localPlayerShip ) {
                return;
            }
            Vector3f camRelative = shipPos.subtract(camera.getLocation());
            float distance = camera.getDirection().dot(camRelative);
            if( distance < 0 ) {
                // It's behind us
                shipLabel.removeFromParent();
                return;
            }
            
            // Calculate the ship's position on screen
            //Vector3f screen1 = camera.getScreenCoordinates(shipPos);
            Vector3f screen2 = camera.getScreenCoordinates(shipPos.add(0, labelOffset, 0));
            
            Vector3f pref = shipLabel.getPreferredSize();
            shipLabel.setLocalTranslation(screen2.x - pref.x * 0.5f, screen2.y + pref.y, screen2.z);
            if( shipLabel.getParent() == null ) {
                hudLabelRoot.attachChild(shipLabel);
            }               
        }
        
        public void updateSpatial( long time ) {
 
            // Look back in the brief history that we've kept and
            // pull an interpolated value.  To do this, we grab the
            // span of time that contains the time we want.  PositionTransition
            // represents a starting and an ending pos+rot over a span of time.
            PositionTransition3f trans = buffer.getTransition(time);
            if( trans != null ) {
                spatial.setLocalTranslation(trans.getPosition(time, true));
                spatial.setLocalRotation(trans.getRotation(time, true));
                setVisible(trans.getVisibility(time));
                
                updateLabelPos(spatial.getWorldTranslation());
            }            
        }
                
        public void addFrame( long endTime, Vector3f pos, Quaternion quat, boolean visible ) {
            PositionTransition3f trans = new PositionTransition3f(endTime, pos, quat, visible);
            buffer.addTransition(trans);
        }        
 
        public PositionTransition3f getFrame( long time ) {
            return buffer.getTransition(time);        
        }
        
        public void dispose() {
            spatial.removeFromParent();
            shipLabel.removeFromParent();
        }
    }
     
    private class GameSessionObserver implements GameSessionListener {
 
        @Override
        public void playerJoined( int clientId, String playerName, int shipId ){
            log.info("playerJoined(" + clientId + ", " + playerName + ", " + shipId + ")");
            playerIndex.put(shipId, new PlayerInfo(clientId, playerName, shipId));
 
            // See if the ship already exists and if so, set its name... this is
            // just in case we get the join message after we receive position
            // updates.
            // But don't force-create the ship or we'll end up with misplaced
            // ghosts.
            ObjectInfo shipInfo = getObjectInfo(shipId, false);
            if( shipInfo != null ) {
                shipInfo.setPlayerName(playerName);
            }          
        }
    
        @Override
        public void playerLeft( int clientId, String playerName, int shipId ) {           
            log.info("playerLeft(" + clientId + ", " + playerName + ", " + shipId + ")");        
            playerIndex.remove(shipId);
        }
    }
    
    /**
     *  Updates our local object views... could/should be split out into
     *  a separate class but it's convenient here for now.  When this code
     *  moves to an ES, we'll break this all apart but for now we are modifying
     *  spatials directly (essentially) and so this is the easiest way.  
     */
    private class SharedObjectUpdater implements SharedObjectListener {
    
        // Time of the current frame.
        private long frameTime;
    
        public SharedObjectUpdater() {
        } 
        
        @Override
        public void beginFrame( long time ) {
            if( log.isTraceEnabled() ) {
                log.trace("** beginFrame(" + time + ")");
            }    
            this.frameTime = time;
        }
    
        @Override
        public void objectUpdated( SharedObject obj ) {
            if( log.isTraceEnabled() ) {
                log.trace("****** Object moved[t=" + frameTime + "]:" + obj.getEntityId() + "  pos:" + obj.getWorldPosition() + "  removed:" + obj.isMarkedRemoved());    
            }
            
            ObjectInfo info = getObjectInfo(obj.getEntityId().intValue(), true); 
            Vector3f pos = obj.getWorldPosition().toVector3f();
            Quaternion quat = obj.getWorldRotation().toQuaternion();                
            info.updatePos = pos;
            info.updateRot = quat;
 
            info.addFrame(frameTime, pos, quat, true);
        }

        @Override
        public void objectRemoved( SharedObject obj ) {
            if( log.isTraceEnabled() ) {
                log.trace("****** Object removed[t=" + frameTime + "]:" + obj.getEntityId());
            }
            
            ObjectInfo info = getObjectInfo(obj.getEntityId().intValue(), false);
            if( info != null ) {
                // The object has been removed in the 'now' but we need to keep
                // it until it's history is used up. 
                info.addFrame(frameTime,  
                              obj.getWorldPosition().toVector3f(), 
                              obj.getWorldRotation().toQuaternion(),
                              false);
 
                // Note that we don't actually delay removal here but if we had
                // an ES then the actual entity removal would come later.  We
                // build everything as if we did, though, and that also means
                // we support history-buffered visibility.
              
                toRemove.add(obj.getEntityId().intValue());
            }
        }

        @Override
        public void endFrame() {
            log.trace("** endFrame()");    
            this.frameTime = -1;
        }
    }
}

