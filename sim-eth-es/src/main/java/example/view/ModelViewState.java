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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.mathd.trans.PositionTransition3f;
import com.simsilica.mathd.trans.TransitionBuffer;

import example.ConnectionState;
import example.GameSessionState;
import example.Main;
import example.TimeState;
import example.es.BodyPosition;
import example.es.ObjectType;
import example.es.ObjectTypes;
import example.es.Position;
import example.es.SphereShape;

/**
 *  Displays the models for the various physics objects.
 *
 *  @author    Paul Speed
 */
public class ModelViewState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(ModelViewState.class);

    private EntityData ed;
    private TimeState timeState;
    
    private Node modelRoot;
 
    private Map<EntityId, Spatial> modelIndex = new HashMap<>();
    
    private MobContainer mobs;
    private ModelContainer models;

    public ModelViewState() {
    }

    public Spatial getModel( EntityId id ) {
        return modelIndex.get(id);
    }

    @Override
    protected void initialize( Application app ) {
        modelRoot = new Node();
        
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
        this.timeState = getState(TimeState.class);
    
        this.ed = getState(ConnectionState.class).getEntityData();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        
        mobs = new MobContainer(ed);
        models = new ModelContainer(ed);
        mobs.start(); 
        models.start();
    
        ((Main)getApplication()).getRootNode().attachChild(modelRoot);
    }

    @Override
    protected void onDisable() {
        modelRoot.removeFromParent();

        models.stop();
        mobs.stop();        
        mobs = null;
        models = null;        
    }

    @Override
    public void update( float tpf ) {
 
        // Grab a consistent time for this frame
        long time = timeState.getTime();

        // Update all of the models
        models.update();
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
        //Material mat = GuiGlobals.getInstance().createMaterial(texture, false).getMaterial();
        Material mat = new Material(getApplication().getAssetManager(), "MatDefs/FogUnshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.setColor("FogColor", new ColorRGBA(0, 0, 0.1f, 1));        
        mat.setFloat("FogDepth", 64);        
        ship.setMaterial(mat);
 
        Node result = new Node("ship:" + entity.getId());
        result.attachChild(ship);        
 
        result.setUserData("entityId", entity.getId().getId());
        
        return result;
    }

    protected Spatial createGravSphere( Entity entity ) {
        
        SphereShape shape = ed.getComponent(entity.getId(), SphereShape.class);
        float radius = shape == null ? 1 : (float)shape.getRadius();
                 
        GuiGlobals globals = GuiGlobals.getInstance(); 
        Sphere sphere = new Sphere(40, 40, radius);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        sphere.scaleTextureCoordinates(new Vector2f(60, 40));
        Geometry geom = new Geometry("test", sphere);
        Texture texture = globals.loadTexture("Textures/gravsphere.png", true, true);
        //Material mat = globals.createMaterial(texture, false).getMaterial();
        Material mat = new Material(getApplication().getAssetManager(), "MatDefs/FogUnshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.setColor("FogColor", new ColorRGBA(0, 0, 0.1f, 1));        
        mat.setFloat("FogDepth", 256);        
        geom.setMaterial(mat);
        
        geom.setLocalTranslation(16, 16, 16);
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        
        return geom;
    }

    protected Spatial createModel( Entity entity ) {
        // Check to see if one already exists
        Spatial result = modelIndex.get(entity.getId());
        if( result != null ) {
            return result;
        }
        
        // Else figure out what type to create... 
        ObjectType type = entity.get(ObjectType.class);
        String typeName = type.getTypeName(ed);
        switch( typeName ) {
            case ObjectTypes.SHIP:
                result = createShip(entity);
                break;
            case ObjectTypes.GRAV_SPHERE:
                result = createGravSphere(entity);
                break;
            default:
                throw new RuntimeException("Unknown spatial type:" + typeName); 
        }
        
        // Add it to the index
        modelIndex.put(entity.getId(), result);
 
        modelRoot.attachChild(result);       
        
        return result;        
    }

    protected void updateModel( Spatial spatial, Entity entity, boolean updatePosition ) {
        if( updatePosition ) {
            Position pos = entity.get(Position.class);
            
            // I like to move it... move it...
            spatial.setLocalTranslation(pos.getLocation().toVector3f());
            spatial.setLocalRotation(pos.getFacing().toQuaternion());
        }
    }
    
    protected void removeModel( Spatial spatial, Entity entity ) { 
        modelIndex.remove(entity.getId());
        spatial.removeFromParent();
    }
    
    private class Mob {
        Entity entity;
        Spatial spatial;
        boolean visible;
        boolean localPlayerShip;
 
        TransitionBuffer<PositionTransition3f> buffer;
        
        public Mob( Entity entity ) {
            this.entity = entity;

            this.spatial = createModel(entity); //createShip(entity);
            //modelRoot.attachChild(spatial);
 
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
            }
 
            // Starts invisible until we know otherwise           
            resetVisibility();
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
            }            
        }
        
        protected void updateComponents() {
            updateModel(spatial, entity, false);
        }
        
        protected void setVisible( boolean f ) {
            if( this.visible == f ) {
                return;
            }
            this.visible = f;
            resetVisibility();
        }
        
        protected void resetVisibility() {        
            if( visible && !localPlayerShip ) {
                spatial.setCullHint(Spatial.CullHint.Inherit);
            } else {
                spatial.setCullHint(Spatial.CullHint.Always);
            }
        }
        
        public void dispose() { 
            if( models.getObject(entity.getId()) == null ) {
                removeModel(spatial, entity);
            }
        }
    }
    
    private class MobContainer extends EntityContainer<Mob> {
        @SuppressWarnings("unchecked")
        public MobContainer( EntityData ed ) {
            super(ed, ObjectType.class, BodyPosition.class);
        }
    
        @Override     
        protected Mob[] getArray() {
            return super.getArray();
        }
    
        @Override       
        protected Mob addObject( Entity e ) {
System.out.println("MobContainer.addObject(" + e + ")");        
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

    /**
     *  Contains the static objects... care needs to be taken that if
     *  an object exists in both the MobContainer and this one that the
     *  MobContainer takes precedence.
     */
    private class ModelContainer extends EntityContainer<Spatial> {
        @SuppressWarnings("unchecked")
        public ModelContainer( EntityData ed ) {
            super(ed, ObjectType.class, Position.class);
        }
        
        @Override       
        protected Spatial addObject( Entity e ) {
System.out.println("ModelContainer.addObject(" + e + ")");
            Spatial result = createModel(e);
            updateObject(result, e);
            return result;        
        }
    
        @Override       
        protected void updateObject( Spatial object, Entity e ) {
System.out.println("MobContainer.updateObject(" + e + ")");        
            updateModel(object, e, true);
        }
    
        @Override       
        protected void removeObject( Spatial object, Entity e ) {
            if( mobs.getObject(e.getId()) == null ) {
                removeModel(object, e);
            }
        }            
        
    }
}

