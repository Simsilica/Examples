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
import com.jme3.renderer.Camera;
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
import com.simsilica.ethereal.TimeSource; 

import com.simsilica.es.*;

import com.simsilica.mathd.trans.PositionTransition;
import com.simsilica.mathd.trans.TransitionBuffer;

import example.ConnectionState;
import example.GameSessionState;
import example.Main;
import example.es.BodyPosition;
import example.net.GameSessionListener;
import example.net.client.GameSessionClientService;

/**
 *  Displays the models for the various physics objects.
 *
 *  @author    Paul Speed
 */
public class ModelViewState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(ModelViewState.class);

    private EntityData ed;
    private TimeSource timeSource;
    
    private Node modelRoot;
    private Node hudLabelRoot;
    private Camera camera;
    
    private MobContainer mobs;

    public ModelViewState() {
    }

    public Spatial getModel( EntityId id ) {
        Mob mob = mobs.getObject(id);
        if( mob == null ) {
            return null;
        }
        return mob.spatial;
    }

    @Override
    protected void initialize( Application app ) {
        modelRoot = new Node();
        hudLabelRoot = new Node("HUD labels");
        
        this.camera = app.getCamera();
 
        // Retrieve the time source from the network connection
        // The time source will give us a time in recent history that we should be
        // viewing.  This currently defaults to -100 ms but could vary (someday) depending
        // on network connectivity.
        // For more information on this interpolation approach, see the Valve networking
        // articles at:
        // https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking
        // https://developer.valvesoftware.com/wiki/Latency_Compensating_Methods_in_Client/Server_In-game_Protocol_Design_and_Optimization
        this.timeSource = getState(ConnectionState.class).getRemoteTimeSource();
    
        this.ed = getState(ConnectionState.class).getEntityData();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        
        mobs = new MobContainer(ed);
        mobs.start();
    
        ((Main)getApplication()).getRootNode().attachChild(modelRoot);
        ((Main)getApplication()).getGuiNode().attachChild(hudLabelRoot);
    }

    @Override
    protected void onDisable() {
        modelRoot.removeFromParent();
        hudLabelRoot.removeFromParent();
        
        mobs.stop();
        mobs = null;
    }

    @Override
    public void update( float tpf ) {
 
        // Grab a consistent time for this frame
        long time = timeSource.getTime();

        // Update all of the models
        mobs.update();
        for( Mob mob : mobs.getArray() ) {
            mob.updateSpatial(time);
        } 
    }
    
    protected Spatial createShip( Entity entity ) {
    
        AssetManager assetManager = getApplication().getAssetManager();
        
        Spatial ship = assetManager.loadModel("Models/fighter.j3o");
        ship.center();
        Texture texture = assetManager.loadTexture("Textures/ship1.png");
        Material mat = GuiGlobals.getInstance().createMaterial(texture, false).getMaterial();
        ship.setMaterial(mat);
 
        Node result = new Node("ship:" + entity.getId());
        result.attachChild(ship);        
 
        result.setUserData("entityId", entity.getId().getId());
        
        return result;
    }
    
    private class Mob {
        Entity entity;
        Spatial spatial;
        boolean visible;
        boolean localPlayerShip;
 
        TransitionBuffer<PositionTransition> buffer;
        
        public Mob( Entity entity ) {
            this.entity = entity;

            this.spatial = createShip(entity);
            modelRoot.attachChild(spatial);
 
            BodyPosition bodyPos = entity.get(BodyPosition.class);
            // BodyPosition requires special management to make
            // sure all instances of BodyPosition are sharing the same
            // thread-safe history buffer.  Everywhere it's used, it should
            // be 'initialized'.            
            bodyPos.initialize(entity.getId(), 12);
            buffer = bodyPos.getBuffer();
            
            // If this is the player's ship then we don't want the model
            // shown else it looks bad.  A) it's ugly.  B) the model will
            // always lag the player's turning.
            if( entity.getId().getId() == getState(GameSessionState.class).getShipId().getId() ) {
                this.localPlayerShip = true;
                spatial.setCullHint(Spatial.CullHint.Always);
            }
        }
 
        public void updateSpatial( long time ) {
 
            // Look back in the brief history that we've kept and
            // pull an interpolated value.  To do this, we grab the
            // span of time that contains the time we want.  PositionTransition
            // represents a starting and an ending pos+rot over a span of time.
            PositionTransition trans = buffer.getTransition(time);
            if( trans != null ) {
                spatial.setLocalTranslation(trans.getPosition(time, true));
                spatial.setLocalRotation(trans.getRotation(time, true));
                setVisible(trans.getVisibility(time));
            }            
        }
        
        protected void updateComponents() {
        }
        
        protected void setVisible( boolean f ) {
            if( this.visible == f ) {
                return;
            }
            this.visible = f;
            if( visible && !localPlayerShip ) {
                spatial.setCullHint(Spatial.CullHint.Inherit);
            } else {
                spatial.setCullHint(Spatial.CullHint.Always);
            }
        }
        
        public void dispose() {
            spatial.removeFromParent();
        }
    }
    
    private class MobContainer extends EntityContainer<Mob> {
        public MobContainer( EntityData ed ) {
            super(ed, BodyPosition.class);
        }
    
        @Override     
        protected Mob[] getArray() {
            return super.getArray();
        }
    
        @Override       
        protected Mob addObject( Entity e ) {
            return new Mob(e);
        }
    
        @Override       
        protected void updateObject( Mob object, Entity e ) {
            object.updateComponents();
        }
    
        @Override       
        protected void removeObject( Mob object, Entity e ) {
            object.dispose();   
        }            
    }

}

