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

package sigem.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;

import sigem.es.Position;

/**
 *  This system clips all positioned entities to the arena bounds, wrapping
 *  their coordinates as necessary.
 *
 *  @author    Paul Speed
 */
public class ArenaBoundary extends AbstractGameSystem  
                           implements PhysicsListener {

    static Logger log = LoggerFactory.getLogger(PlanetGravity.class);

    private EntityData ed;
    private Vec3d bounds;
    private ObjectContainer objects;    

    public ArenaBoundary( Vec3d bounds ) {
        this.bounds = bounds; 
    }
 
    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class, true);
        getSystem(SimplePhysics.class).addPhysicsListener(this);
    }

    @Override
    protected void terminate() {
        getSystem(SimplePhysics.class).removePhysicsListener(this);
    }

    @Override
    public void start() {
        objects = new ObjectContainer(ed);
        objects.start();
    }

    @Override
    public void stop() {
        objects.stop();
        objects = null;
    }

    @Override
    public void update( SimTime time ) {
        objects.update();
    }

    private boolean clamp( Vec3d v, Vec3d target ) {
        boolean changed = false;
        target.set(v);
        if( v.x < -bounds.x ) {
            target.x = v.x + bounds.x * 2;
            changed = true;
        } else if( v.x > bounds.x ) {
            target.x = v.x - bounds.x * 2;
            changed = true;
        }
        if( v.z < -bounds.z ) {
            target.z = v.z + bounds.z * 2;
            changed = true;
        } else if( v.z > bounds.z ) {
            target.z = v.z - bounds.z * 2;
            changed = true;
        }
        if( v.y != bounds.y ) {
            target.y = bounds.y;
            changed = true;
        }
        return changed;  
    } 
 
    @Override
    public void beginFrame( SimTime time ) {
    }
 
    @Override
    public void addBody( Body body ) {    
    }
 
    /**
     *  Clamp the physics objects directly... the entity will get
     *  clamped by the ObjectContainer.
     */   
    @Override
    public void updateBody( Body body ) {
        clamp(body.pos, body.pos);
    }
 
    @Override
    public void removeBody( Body body ) {
    }

    @Override
    public void endFrame( SimTime time ) {
    }
    
    private class ObjectContainer extends EntityContainer<Entity> {
 
        private Vec3d temp = new Vec3d();
 
        @SuppressWarnings("unchecked")
		public ObjectContainer( EntityData ed ) {
            super(ed, Position.class);
        }
        
        @Override     
        protected Entity[] getArray() {
            return super.getArray();
        }
    
        @Override     
        protected Entity addObject( Entity e ) {
            updateObject(e, e);
            return e;
        }
    
        @Override     
        protected void updateObject( Entity object, Entity e ) {
            Position pos = object.get(Position.class);
            Vec3d loc = pos.getLocation();
            if( clamp(loc, temp) ) {
                object.set(pos.changeLocation(temp.clone()));
            }
        }
    
        @Override     
        protected void removeObject( Entity object, Entity e ) {
        }                   
    }    
    
}


