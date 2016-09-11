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

package example.sim;

import java.util.*;
import java.util.concurrent.*;

import com.jme3.util.SafeArrayList;

import com.simsilica.es.*;
import com.simsilica.sim.*;

import example.es.*;

/**
 *  Just a basic physics simulation that integrates acceleration, 
 *  velocity, and position on "point masses".
 *
 *  @author    Paul Speed
 */
public class SimplePhysics extends AbstractGameSystem {

    private EntityData ed;
    
    // Single threaded.... we'll have to take care when adding/removing
    // items.
    private SafeArrayList<Body> bodies = new SafeArrayList<>(Body.class);
    private Map<EntityId, Body> index = new ConcurrentHashMap<>();
    
    private ConcurrentLinkedQueue<Body> toAdd = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Body> toRemove = new ConcurrentLinkedQueue<>();
 
    private SafeArrayList<PhysicsListener> listeners = new SafeArrayList<>(PhysicsListener.class);
    
    public SimplePhysics() {
    }
 
    /**
     *  Adds a listener that will be notified about physics related updates.
     *  This is not a thread safe method call so must be called during setup
     *  or from the physics/simulation thread.
     */   
    public void addPhysicsListener( PhysicsListener l ) {
        listeners.add(l);
    }

    public void removePhysicsListener( PhysicsListener l ) {
        listeners.remove(l);
    }
 
    public Body getBody( EntityId entityId, boolean create ) {
        Body result = index.get(entityId);
        if( result == null && create ) {
            synchronized(this) {
                result = index.get(entityId);
                if( result != null ) {
                    return result;
                }
                result = new Body(entityId);
                toAdd.add(result);
                index.put(entityId, result);         
            }
        }
        return result;
    }
    
    public boolean removeBody( EntityId entityId ) {
        Body result = index.remove(entityId);
        if( result != null ) {
            toRemove.add(result);
            return true;
        }
        return false;
    }
    
    protected void initialize() {
        this.ed = getSystem(EntityData.class);
        if( ed == null ) {
            throw new RuntimeException("SimplePhysics system requires an EntityData object.");
        }
    }
    
    protected void terminate() {
    }

    private void updateBodyList() {
        if( !toAdd.isEmpty() ) {
            Body body = null;
            while( (body = toAdd.poll()) != null ) {
                bodies.add(body);
                for( PhysicsListener l : listeners.getArray() ) {
                    l.addBody(body);
                }
            }
        }
        if( !toRemove.isEmpty() ) { 
            Body body = null;
            while( (body = toRemove.poll()) != null ) {
                bodies.remove(body);
                for( PhysicsListener l : listeners.getArray() ) {
                    l.removeBody(body);
                }
            }
        } 
    }

    @Override
    public void update( SimTime time ) {
 
        for( PhysicsListener l : listeners.getArray() ) {
            l.beginFrame(time);
        }
        
        updateBodyList();
 
        double tpf = time.getTpf();
 
        // Apply control driver changes
        for( Body b : bodies.getArray() ) {
            if( b.driver != null ) {
                b.driver.update(tpf, b);
            }
        }
 
        // Integrate
        for( Body b : bodies.getArray() ) {
            b.integrate(tpf);
        }
 
        // Publish the results
        for( PhysicsListener l : listeners.getArray() ) {
            for( Body b : bodies.getArray() ) {
                l.updateBody(b);
            }
        }
               
        for( PhysicsListener l : listeners.getArray() ) {
            l.endFrame(time);
        }
    }

    /**
     *  Maps the appropriate entities to physics bodies.
     */
    private class BodyContainer extends EntityContainer<Body> {
 
        public BodyContainer( EntityData ed ) {
            super(ed, Position.class, MassProperties.class, SphereShape.class);
        }
        
        protected Body addObject( Entity e ) {
            Body newBody = getBody(e.getId(), true);
            
            Position pos = e.get(Position.class);
            newBody.setPosition(pos);
            return newBody;
        }
    
        protected void updateObject( Body object, Entity e ) {
            // We don't support live-updating mass or shape right now
        }
    
        protected void removeObject( Body object, Entity e ) {
            removeBody(e.getId());
        }    
               
    }    
}


