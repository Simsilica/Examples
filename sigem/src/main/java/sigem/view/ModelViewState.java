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

package sigem.view;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.*;
import com.jme3.texture.Texture;
import com.jme3.util.SafeArrayList;

import com.simsilica.es.*;
import com.simsilica.lemur.*;
import com.simsilica.lemur.anim.*;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.mathd.*;
import com.simsilica.state.*;

import sigem.Main;
import sigem.es.*;

/**
 *  Displays the models for the various physics objects.
 *
 *  @author    Paul Speed
 */
public class ModelViewState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(ModelViewState.class);

    private EntityData ed;
    private AssetManager assets;
    
    private Node modelRoot;
 
    private Map<EntityId, Spatial> modelIndex = new HashMap<>();
    
    private ModelContainer models;
    
    private Vec3d minClip = new Vec3d();
    private Vec3d maxClip = new Vec3d();

    public ModelViewState() {
    }

    public Vec3d getMinClip() {
        return minClip;
    }
    
    public Vec3d getMaxClip() {
        return maxClip;
    }

    public Spatial getModel( EntityId id ) {
        return modelIndex.get(id);
    }

    @Override
    protected void initialize( Application app ) {
        modelRoot = new Node();
        assets = app.getAssetManager();
        
        this.ed = getState(GameSystemsState.class).get(EntityData.class);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        
        models = new ModelContainer(ed);
        models.start();
        
        ((Main)getApplication()).getRootNode().attachChild(modelRoot);
    }

    @Override
    protected void onDisable() {
        modelRoot.removeFromParent();

        models.stop();        
        models = null;                
    }

    @Override
    public void update( float tpf ) { 
        // Update all of the models
        models.update();
        
        // Clamp the spatials to the current viewport
        for( Spatial s : models.getArray() ) {
            Vector3f v = s.getLocalTranslation().clone();
            if( v.x < minClip.x ) {
                v.x = (float)(maxClip.x - (minClip.x - v.x));
            } else if( v.x > maxClip.x ) {
                v.x = (float)(minClip.x + (v.x - maxClip.x));
            }
            if( v.z < minClip.z ) {
                v.z = (float)(maxClip.z - (minClip.z - v.z));
            } else if( v.z > maxClip.z ) {
                v.z = (float)(minClip.z + (v.z - maxClip.z));
            }
            s.setLocalTranslation(v);
 
            /*if( s instanceof Node ) {           
                Spatial child = ((Node)s).getChild("rock");
                if( child != null ) {
                    log.info("child loc:" + child.getLocalTranslation());
                }
            }*/
        }
    }
    
    protected Spatial createShip( Entity entity ) {
    
        Spatial ship = assets.loadModel("Models/fighter.j3o");
        ship.center();
        Texture texture = assets.loadTexture("Textures/ship1.png");
        Material mat = GuiGlobals.getInstance().createMaterial(texture, false).getMaterial();
        mat.setTexture("ColorMap", texture);
        ship.setMaterial(mat);
 
        Node result = new Node("ship:" + entity.getId());
        result.attachChild(ship);        
 
        result.setUserData("entityId", entity.getId().getId());

        SphereShape shape = ed.getComponent(entity.getId(), SphereShape.class);
        if( shape != null ) {
            // Add a debug shape for a sec
            float radius = (float)shape.getRadius();
            
            Sphere sphere = new Sphere(3, 10, radius);
            Geometry geom = new Geometry("debug", sphere);            
            mat = GuiGlobals.getInstance().createMaterial(new ColorRGBA(1, 1, 0, 0.25f), false).getMaterial();
            mat.getAdditionalRenderState().setWireframe(true);
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            geom.setMaterial(mat);
            geom.rotate(FastMath.HALF_PI, 0, 0);
            geom.setQueueBucket(Bucket.Transparent);
            geom.setCullHint(CullHint.Always);
            result.attachChild(geom);
        }
        
        return result;
    }

    protected Spatial createPlanet( Entity entity ) {
        SphereShape shape = ed.getComponent(entity.getId(), SphereShape.class);
        float radius = shape == null ? 1 : (float)shape.getRadius();
        
        GuiGlobals globals = GuiGlobals.getInstance(); 
        Sphere sphere = new Sphere(40, 40, radius);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        //sphere.scaleTextureCoordinates(new Vector2f(60, 40));
        Geometry geom = new Geometry("planet", sphere);
        Texture texture = globals.loadTexture("Textures/earthmap.jpg", true, true);
        Material mat = globals.createMaterial(texture, false).getMaterial();
        //Material mat = globals.createMaterial(ColorRGBA.Blue, false).getMaterial();
        //Material mat = new Material(getApplication().getAssetManager(), "MatDefs/Unshaded.j3md");
        //mat.setTexture("ColorMap", texture);
        //mat.setColor("FogColor", new ColorRGBA(0, 0, 0.1f, 1));        
        //mat.setFloat("FogDepth", 256);        
        geom.setMaterial(mat);
        
        geom.setLocalTranslation(16, 16, 16);
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        
        return geom;
    }

    protected Spatial createAsteroid( Entity entity ) {
    
        Spatial rock = assets.loadModel("Models/Rock1/Rock1.j3o");
        rock.setName("rock");
        //rock.setLocalScale(50);
        rock.center();
        Texture texture = assets.loadTexture("Models/Rock1/textures/Rock02LV3_1_1024.jpg");
        Material mat = GuiGlobals.getInstance().createMaterial(texture, false).getMaterial();
        //mat.setTexture("ColorMap", texture);
        rock.setMaterial(mat);
 
        Node result = new Node("asteroid:" + entity.getId());
        result.attachChild(rock);        
 
        result.setUserData("entityId", entity.getId().getId());

        SphereShape shape = ed.getComponent(entity.getId(), SphereShape.class);
        if( shape != null ) {        
            // Add a debug shape for a sec
            float radius = (float)shape.getRadius();
            rock.setLocalScale(radius / 3);
            
            Sphere sphere = new Sphere(3, 10, radius);
            Geometry geom = new Geometry("debug", sphere);            
            mat = GuiGlobals.getInstance().createMaterial(new ColorRGBA(1, 1, 0, 0.25f), false).getMaterial();
            mat.getAdditionalRenderState().setWireframe(true);
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            geom.setMaterial(mat);
            geom.rotate(FastMath.HALF_PI, 0, 0);
            geom.setQueueBucket(Bucket.Transparent);
            geom.setCullHint(CullHint.Always);
            result.attachChild(geom);
        }
        
        return result;
    }
    
    protected Geometry createQuad( float size, String asset, BlendMode blendMode ) {
        GuiGlobals globals = GuiGlobals.getInstance(); 
        Quad quad = new Quad(size, size);
        Geometry geom = new Geometry("quad", quad);
        Texture texture = globals.loadTexture(asset, false, false);
        //Material mat = globals.createMaterial(ColorRGBA.Blue, false).getMaterial();
        Material mat = globals.createMaterial(texture, false).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(blendMode);
        geom.setQueueBucket(Bucket.Transparent);
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        geom.setMaterial(mat);
        geom.center();
        return geom;
    }
    
    protected Spatial createThrust( Entity entity ) {
     
        Node result = new Node("thrust");    
        Geometry geom = createQuad(2, "Textures/neon-puff256.png", BlendMode.AlphaAdditive);
        geom.move(0, -1, 0);
        Material mat = geom.getMaterial();
        
        ColorRGBA color = new ColorRGBA(2, 2, 0, 1);
        mat.setColor("Color", color);
        
        // Fade it out over five seconds.  We could have created a system to do
        // this based on decay, blah blah... but this is really easy.
        //getState(AnimationState.class).add(new ColorTween(color, ColorRGBA.White, new ColorRGBA(1, 1, 1, 0), 5));
        getState(AnimationState.class).add(new ColorTween(color, ColorRGBA.White, new ColorRGBA(1, 1, 1, 0), 2.5));
        
        result.attachChild(geom);
        
        return result;   
    }

    protected Spatial createMissile( Entity entity ) {
     
        Node result = new Node("thrust");    
        Geometry geom = createQuad(4, "Textures/missile.png", BlendMode.AlphaAdditive);
        geom.move(0, -1, 0);
        Material mat = geom.getMaterial();
        
        //ColorRGBA color = new ColorRGBA(2, 2, 0, 1);
        //mat.setColor("Color", color);
        
        // Fade it out over five seconds.  We could have created a system to do
        // this based on decay, blah blah... but this is really easy.
        //getState(AnimationState.class).add(new ColorTween(color, ColorRGBA.White, new ColorRGBA(1, 1, 1, 0), 5));
        
        result.attachChild(geom);
        
        return result;   
    }

    protected Spatial createPlasmaExplosion( Entity entity ) {
     
        Node result = new Node("thrust");    
        Geometry geom = createQuad(4, "Textures/white-puff256.png", BlendMode.AlphaAdditive);
        geom.move(0, -1, 0);
        Material mat = geom.getMaterial();
        
        ColorRGBA color = new ColorRGBA(2, 1, 1, 2);
        ColorRGBA color1 = new ColorRGBA(2, 1, 1, 2);
        ColorRGBA color2 = new ColorRGBA(2, 1, 1, 0);
        mat.setColor("Color", color);
        
        // Fade it out over five seconds.  We could have created a system to do
        // this based on decay, blah blah... but this is really easy.
        //getState(AnimationState.class).add(new ColorTween(color, ColorRGBA.White, new ColorRGBA(1, 1, 1, 0), 5));
        getState(AnimationState.class).add(new ColorTween(color, color1, color2, 2));
        
        result.attachChild(geom);
        
        return result;   
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
            case ObjectType.TYPE_SHIP:
                result = createShip(entity);
                break;
            case ObjectType.TYPE_PLANET:
                result = createPlanet(entity);
                break;
            case ObjectType.TYPE_ASTEROID:
            case ObjectType.TYPE_ASTEROID_CHUNK:
                result = createAsteroid(entity);
                break;
            case ObjectType.TYPE_THRUST:
                result = createThrust(entity);        
                break;
            case ObjectType.TYPE_MISSILE:
                result = createMissile(entity);
                break;
            case ObjectType.TYPE_PLASMA_EXPLOSION:
                result = createPlasmaExplosion(entity);
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
    
    /**
     *  Contains the static objects... care needs to be taken that if
     *  an object exists in both the MobContainer and this one that the
     *  MobContainer takes precedence.
     */
    private class ModelContainer extends EntityContainer<Spatial> {
        public ModelContainer( EntityData ed ) {
            super(ed, ObjectType.class, Position.class);
        }
        
        public Spatial[] getArray() {
            return super.getArray();
        }
        
        @Override       
        protected Spatial addObject( Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("addObject(" + e + ")");
            }
            Spatial result = createModel(e);
            updateObject(result, e);
            return result;        
        }
    
        @Override       
        protected void updateObject( Spatial object, Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("updateObject(" + e + ")");
            }        
            updateModel(object, e, true);
        }
    
        @Override       
        protected void removeObject( Spatial object, Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("removeObject(" + e + ")");
            }        
            removeModel(object, e);
        }
    }
    
    /**
     *  Lemur does not yet provide one of these so we will do it ourselves.
     */
    private class ColorTween extends AbstractTween {

        private final ColorRGBA target;
        private final ColorRGBA from;
        private final ColorRGBA to;
        private final ColorRGBA value;

        public ColorTween( ColorRGBA target, ColorRGBA from, ColorRGBA to, double length ) {
            super(length);
            this.target = target;
            this.from = from.clone();
            this.to = to.clone();
            this.value = new ColorRGBA(from);
        }

        @Override
        protected void doInterpolate( double t ) {
            // Interpolate
            value.interpolateLocal(from, to, (float)t);
            target.set(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }

}

