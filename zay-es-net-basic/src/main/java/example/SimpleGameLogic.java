/*
 * $Id$
 * 
 * Copyright (c) 2015, Simsilica, LLC
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

package example;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;

/**
 *
 *
 *  @author    Paul Speed
 */
public class SimpleGameLogic {
    private EntityData ed;
    
    private long step = 0;       
 
    private EntitySet samples;
    
    // Just keep track of how many we created
    private int creationCount = 0;
    
    public SimpleGameLogic( EntityData ed ) {
        this.ed = ed;
        
        // Create some sample entities
        for( int i = 0; i < 10; i++ ) {
            createRandomEntity();
        }
        
        // Grab the entity set for our stuff
        samples = ed.getEntities(Name.class, Position.class);
    }
 
    private EntityId createRandomEntity() {
        EntityId result = ed.createEntity();
        float x = (int)(Math.random() * 10);
        float z = (int)(Math.random() * 10);
            
        int dir = (int)(Math.random() * 4);
        float angle = FastMath.QUARTER_PI * dir;            
        ed.setComponents(result, new Name("Test:" + (++creationCount)),
                         new Position(new Vector3f(x, 0, z), 
                                      new Quaternion().fromAngles(0, angle, 0)));
        return result; 
    }
 
    private void moveEntity( Entity e ) {
        Position pos = e.get(Position.class);
        Vector3f look = pos.getFacing().mult(Vector3f.UNIT_Z);
        Vector3f v = pos.getLocation().clone();
        v.add(look);
 
        boolean bounce = false;       
        if( v.x >= 10 ) {
            v.x = 9;
            bounce = true;
        }
        if( v.z >= 10 ) {
            v.z = 9;
            bounce = true;
        }
        if( v.x < 0 ) {
            v.x = 0;
            bounce = true;
        }
        if( v.z < 0 ) {
            v.z = 0;
            bounce = true;
        }
        if( bounce ) {
            // Randomly change the direction
            int dir = (int)(Math.random() * 4);
            float angle = FastMath.QUARTER_PI * dir;
            pos = new Position(v, new Quaternion().fromAngles(0, angle, 0));  
        } else {
            // Just change the position
            pos = pos.changeLocation(v);
        }
        e.set(pos);
    }
    
    public void update() {
        // The game server calls us 10 times a second
        step++;
 
        // Keep our set up to date
        samples.applyChanges();
        
        for( Entity e : samples ) {
            // Random chance that we will move the entity
            if( Math.random() < 0.25 ) {
                moveEntity(e);
            }
        }
        
        // Every 5 second (50 steps) delete an entity and add another
        if( (step % 50) == 0 ) {
            int markedForDeath = (int)(Math.random() * samples.size());
            for( Entity e : samples ) {
                if( markedForDeath == 0 ) {
                    ed.removeEntity(e.getId());
                    break;
                } 
                markedForDeath--;
            }
 
            createRandomEntity();           
        }
    }
}
