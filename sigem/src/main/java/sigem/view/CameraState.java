/*
 * $Id$
 * 
 * Copyright (c) 2019, Simsilica, LLC
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

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.texture.Texture;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.mathd.*;
import com.simsilica.es.*;
import com.simsilica.state.*;

import sigem.GameConstants;
import sigem.Main;
import sigem.es.*;

/**
 *  Manages the camera location and the background star field based on the
 *  current set of ships.
 *
 *  @author    Paul Speed
 */
public class CameraState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(CameraState.class);

    private Camera cam;
    private EntityData ed;
    
    private Node starfield;
    
    private ShipContainer ships;
    private Vec3d averagePos = new Vec3d();

    private float camHeight = 200;
    private float targetHeight = 200;
    //private float camHeight = 500;
    //private float camHeight = 100;

    // The maximum visible area at full zoom.  When the
    // player ships are farther apart than this then we will have
    // to wrap things.
    private Vec3d maxViewSize = GameConstants.ARENA_EXTENTS; 

    // When the distance between ships is in the range of
    // these entries then the camera is set to their y value.
    private Vec3d[] heightSelection = new Vec3d[] {
            new Vec3d(50, 50, 40),
            new Vec3d(100, 100, 80),
            new Vec3d(100000, 200, 10000)
        };

    public CameraState() {        
    }
    
    @Override
    protected void initialize( Application app ) {

        this.ed = getState(GameSystemsState.class).get(EntityData.class);

        this.cam = app.getCamera();
        cam.setLocation(new Vector3f(0, camHeight, 0));
        cam.setRotation(new Quaternion().fromAngles(FastMath.HALF_PI, FastMath.PI, 0));        
 
        GuiGlobals globals = GuiGlobals.getInstance();

        starfield = new Node("starfield");

        float w = (float)(GameConstants.ARENA_EXTENTS.x * 2);
        float h = (float)(GameConstants.ARENA_EXTENTS.z * 2); 
 
        Quad quad;
        Geometry geom;
        Material mat;
        
        boolean debug = false;
        if( debug ) {
            quad = new Quad(w, h);
            geom = new Geometry("starfield", quad);
            mat = globals.createMaterial(ColorRGBA.Blue, false).getMaterial();
            mat.getAdditionalRenderState().setWireframe(true); 
            geom.setMaterial(mat);
            geom.rotate(-FastMath.HALF_PI, 0, 0);
            geom.center();
            starfield.attachChild(geom);
        }
        
        Texture texture = globals.loadTexture("Textures/starmap.png", true, true);
        quad = new Quad(w * 3, h * 3);
        quad.scaleTextureCoordinates(new Vector2f(6, 6));
        geom = new Geometry("starfield", quad);
        geom.setMaterial(globals.createMaterial(texture, false).getMaterial());
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        geom.center();
        //geom.move(0, 1, 0);
        starfield.attachChild(geom);
        
        //starfield.move(0, -500, 0);
        starfield.move(0, -100, 0);
        
        /*
        // Debug view port sizes
        w = cam.getWidth() * 0.8f;
        h = cam.getHeight();
        quad = new Quad(w, h);
        geom = new Geometry("border", quad);
        mat = globals.createMaterial(ColorRGBA.Blue, false).getMaterial();
        mat.getAdditionalRenderState().setWireframe(true); 
        geom.setMaterial(mat);
        ((Main)getApplication()).getGuiNode().attachChild(geom);*/       
    }
    
    @Override
    protected void cleanup( Application app ) {
    }
    
    @Override
    protected void onEnable() {
        ((Main)getApplication()).getRootNode().attachChild(starfield);
        
        ships = new ShipContainer(ed);
        ships.start();       
    }
    
    @Override
    protected void onDisable() {        
        starfield.removeFromParent();
        
        ships.stop();
        ships = null;
    }
 
    @Override
    public void update( float tpf ) {
        ships.update();
        
        Vec3d min = new Vec3d(Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY);
        Vec3d max = new Vec3d(Double.NEGATIVE_INFINITY, 0, Double.NEGATIVE_INFINITY); 
        for( Entity e : ships.getArray() ) {
            Position pos = e.get(Position.class);
            //log.info("pos:" + pos.getLocation());            
            min.minLocal(pos.getLocation());
            max.maxLocal(pos.getLocation());
        }
        
        // If the span is wider than the max viewable area then we need to
        // wrap the display
        double w = max.x - min.x;
        double d = max.z - min.z;  
         
        if( max.x - min.x > maxViewSize.x ) {
            if( Math.abs(max.x) < Math.abs(min.x) ) {
                // Max is closer to the origin so we'll move min
                min.x += maxViewSize.x * 2;
            } else {
                // Min is closer to the origin so we'll move max
                max.x -= maxViewSize.x * 2;
            }
            w = min.x - max.x;
        }
        if( max.z - min.z > maxViewSize.z ) {
            if( Math.abs(max.z) < Math.abs(min.z) ) {
                // Max is closer to the origin so we'll move min
                min.z += maxViewSize.z * 2;
            } else {
                // Min is closer to the origin so we'll move max
                max.z -= maxViewSize.z * 2;
            }
            d = min.z - max.z;
        }
        
        averagePos.set(max).addLocal(min);
        averagePos.multLocal(0.5);

        // Center the clipping area around the current camera center
        Vec3d minClip = getState(ModelViewState.class).getMinClip();
        Vec3d maxClip = getState(ModelViewState.class).getMaxClip();
        minClip.set(averagePos).subtractLocal(maxViewSize);
        maxClip.set(averagePos).addLocal(maxViewSize);
 
        for( Vec3d selector : heightSelection ) {
            if( w < selector.x && d < selector.z ) {
                targetHeight = (float)selector.y;
                break;
            }
        }       

        if( camHeight < targetHeight ) {
            camHeight = Math.min(targetHeight, camHeight + tpf * 1000);
        } else if( camHeight > targetHeight ) {
            camHeight = Math.max(targetHeight, camHeight - tpf * 1000);
        }

        cam.setLocation(new Vector3f((float)averagePos.x, camHeight, (float)averagePos.z));
    }
 
    /**
     *  So we can track the average position of all of the ships...
     *  this is really just a membership set.
     */   
    private class ShipContainer extends EntityContainer<Entity> {
        public ShipContainer( EntityData ed ) {
            super(ed, ShipInput.class, Position.class);           
        }
 
        @Override     
        protected Entity[] getArray() {
            return super.getArray();
        }
        
        @Override       
        protected Entity addObject( Entity e ) {
            return e;        
        }
    
        @Override       
        protected void updateObject( Entity object, Entity e ) {
        }
    
        @Override       
        protected void removeObject( Entity object, Entity e ) {
        }                    
    }
}
