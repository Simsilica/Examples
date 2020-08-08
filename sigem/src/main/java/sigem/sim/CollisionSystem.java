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

package sigem.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.util.SafeArrayList;
import com.simsilica.es.EntityData;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;

import sigem.GameConstants;


/**
 *  Watches the current set of physics bodies for collisions.
 *
 *  @author    Paul Speed
 */
public class CollisionSystem extends AbstractGameSystem  
                               implements PhysicsListener {
 
    static Logger log = LoggerFactory.getLogger(CollisionSystem.class);
 
    @SuppressWarnings("unused")
    private EntityData ed;
    @SuppressWarnings("unused")
    private SimTime time;
    private Vec3d arenaSize = GameConstants.ARENA_EXTENTS.mult(2);
    
    private SafeArrayList<Body> bodies = new SafeArrayList<>(Body.class);
    
    private SafeArrayList<ContactListener> listeners = new SafeArrayList<>(ContactListener.class);
    
    public CollisionSystem() { 
    }
 
    public void addContactListener( ContactListener l ) {
        listeners.add(l);
    }
    
    public void removeContactListener( ContactListener l ) {
        listeners.remove(l);
    }
 
    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);
        
        getSystem(SimplePhysics.class).addPhysicsListener(this);
    }

    @Override
    protected void terminate() {
        getSystem(SimplePhysics.class).removePhysicsListener(this);
    }

    protected void fireNewContact( Contact c ) {
        for( ContactListener l : listeners.getArray() ) {
            l.newContact(c);
        }
    }
   
    @Override
    public void beginFrame( SimTime time ) {
        this.time = time;
    }
 
    @Override    
    public void addBody( Body body ) {
        bodies.add(body);    
    }
    
    @Override
    public void updateBody( Body body ) {
    }
 
    @Override
    public void removeBody( Body body ) {
        bodies.remove(body);    
    }

    @Override
    public void endFrame( SimTime time ) {
        
        // Check for collisions
        Body[] array = bodies.getArray();
        int size = array.length;
        for( int i = 0; i < size; i++ ) {
            Body b1 = array[i];
            for( int j = i + 1; j < size; j++ ) {
                Body b2 = array[j];
                
                Contact contact = checkContact(b1, b2);
                if( contact == null ) {
                    continue;
                }
                
                // How much energy is in the contact?
                double speed1 = b1.velocity.dot(contact.cn);
                double speed2 = b2.velocity.dot(contact.cn);
                
                // It's possible that the object are already separating
                // if they managed to penetrate really far in a previous frame.
                if( speed1 < 0 && speed2 > 0 ) {
//log.info("separating contact");                
                    continue;
                }
                
                // In a typical collision, speed1 would be positive
                // and speed2 would be negative, ie: both objects
                // heading towards each other.  In that case the total
                // energy is both combined speed2 - speed1.
                // However, if both objects are moving in the same direction
                // and one overtakes the other then the speeds will have the
                // same signs... and we still need the difference.
                double energy = Math.abs(speed2 - speed1);
                contact.energy = energy;
//log.info("speed1:" + speed1 + "  speed2:" + speed2 + "  energy:" + energy);

                // Notiy the listeners about the contact... this gives
                // them a chance to adjust contact parameters before
                // we deal with energy
                fireNewContact(contact);
                if( contact.energy == 0 ) {
                    continue;
                }

                // We want the collisions to be elastic... if we only
                // use the exact energy it will only be enough to stop
                // the objects, not separate them with a bounce.
                energy *= 1.1;

                // Now each side gets some of the energy based on their mass
                // The less mass, the more energy it gets... which is why
                // invMass is convenient.  Also, static objects get 0.
                double totalMass = b1.invMass + b2.invMass;
                double e1 = energy * b1.invMass / totalMass;
                double e2 = energy * b2.invMass / totalMass;
 
//log.info("e1:" + e1 + "  e2:" + e2 + "  cn:" + contact.cn); 
                // Contact normal always points to b2... so we'll
                // subtract from b1's velocity and add to b2's the
                // scaled cn.
                b1.velocity.subtractLocal(contact.cn.mult(e1));
                b2.velocity.addLocal(contact.cn.mult(e2));
            } 
        } 
    
    }
    
    private Contact checkContact( Body b1, Body b2 ) {
        if( b1.invMass == 0 && b2.invMass == 0 ) {
            return null;
        }
        double x = Math.abs(b1.pos.x - b2.pos.x);
        double z = Math.abs(b1.pos.z - b2.pos.z);
        if( x > arenaSize.x ) {
            x -= arenaSize.x;
        }
        if( z > arenaSize.z ) {
            z -= arenaSize.z;
        }
        double dist = Math.sqrt(x * x + z * z);
        if( dist >= b1.radius + b2.radius ) {
            return null;
        } 
        //log.info("contact:" + b1.bodyId + " and:" + b2.bodyId);
        Contact c = new Contact(b1, b2);
        c.cn = b2.pos.subtract(b1.pos);
        c.cn.y = 0; // just in case
        c.cn.divideLocal(dist);
        c.pen = dist - b1.radius - b2.radius;
    
        if( c.pen < 0 ) {       
            // Put the contact point halfway between the bounaries        
            c.cp = b1.pos.add(c.cn.mult(b1.radius + c.pen * 0.5));
        } else {
            // Put the conact point on b1's surface
            c.cp = b1.pos.add(c.cn.mult(b1.radius));
        }        
        
        return c;
    }
}


