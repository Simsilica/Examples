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

import java.util.Random;

import org.slf4j.*;

import com.simsilica.es.*;
import com.simsilica.mathd.*;
import com.simsilica.sim.*;

import sigem.es.*;

/**
 *  Checks for missile-asteroid hits or high energy asteroid
 *  hits and implements asteroid damage.
 *
 *  @author    Paul Speed
 */
public class AsteroidHitListener extends AbstractGameSystem  
                                 implements ContactListener {
                                 
    static Logger log = LoggerFactory.getLogger(AsteroidHitListener.class);
 
    private EntityData ed;
    private GameEntities gameEntities;
    private Random rand = new Random(0);
                                 
    public AsteroidHitListener() {
    }

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class, true);
        this.gameEntities = getSystem(GameEntities.class, true);
        
        getSystem(CollisionSystem.class).addContactListener(this);
    }

    @Override
    protected void terminate() {
        getSystem(CollisionSystem.class).removeContactListener(this);
    }

    @Override
    public void newContact( Contact c ) {
        ObjectType type1 = ed.getComponent(c.b1.bodyId, ObjectType.class);
        ObjectType type2 = ed.getComponent(c.b2.bodyId, ObjectType.class);
        
        String t1 = type1 == null ? null : type1.getTypeName(ed);
        String t2 = type2 == null ? null : type2.getTypeName(ed);  
        
        if( !ObjectType.TYPE_ASTEROID.equals(t1) && !ObjectType.TYPE_ASTEROID.equals(t2) ) {
            // Neither side is an asteroid... nothing to do
            return;
        }

        // If either side is a chunk then we'll negate the energy
        if( ObjectType.TYPE_ASTEROID_CHUNK.equals(t1) || ObjectType.TYPE_ASTEROID_CHUNK.equals(t2) ) {
            c.energy = 0;
            return;
        } 
        
        //log.info("contact:" + c + "  types:" + t1 + ", " + t2);        
        
        if( ObjectType.TYPE_MISSILE.equals(t1) ) {
            // Blow up b2
            missileHit(c.b1, c.b2, c);
        } else if( ObjectType.TYPE_MISSILE.equals(t2) ) {
            // Blow up b1
            missileHit(c.b2, c.b1, c);
        } 
        
    }
    
    protected void missileHit( Body missile, Body asteroid, Contact contact ) {
        EntityId shooter = ed.getComponent(missile.bodyId, CreatedBy.class).getCreatorId();
        log.info("Shooter:" + shooter + "  name:" + ed.getComponent(shooter, Name.class));
 
        Vec3d debrisLoc = missile.pos;
        
        if( asteroid.radius > 1 ) {
            // Need to break it up
            
            // Figure out which way the pieces go
            Vec3d offset = contact.cn.cross(Vec3d.UNIT_Y);
            offset.multLocal(1 + rand.nextDouble());
            
            double size = Math.max(1, asteroid.radius / 2);
 
            Vec3d loc1 = asteroid.pos.add(offset.mult(asteroid.radius * 0.5));
            Vec3d loc2 = asteroid.pos.subtract(offset.mult(asteroid.radius * 0.5));
 
            EntityId ast1 = gameEntities.createAsteroid(loc1, offset.add(asteroid.velocity), 
                                        new Vec3d(rand.nextDouble() + 1, rand.nextDouble(), 0),
                                        size);
            EntityId ast2 = gameEntities.createAsteroid(loc2, offset.subtract(asteroid.velocity), 
                                        new Vec3d(rand.nextDouble() + 1, rand.nextDouble(), 0),
                                        size);
            
        } else {
            debrisLoc = asteroid.pos;
        }

        // Create some asteroid debris
        int count = rand.nextInt((int)(3 * asteroid.radius)) + 3;
        for( int i = 0; i < count; i++ ) {
            double x = rand.nextDouble() * 2 - 1;
            double z = rand.nextDouble() * 2 - 1;
            Vec3d dir = new Vec3d(x, 0, z);
            double size = 0.2 + rand.nextDouble() * 0.4;
            
            EntityId chunk = gameEntities.createAsteroidChunk(debrisLoc.add(dir), dir,
                                        new Vec3d(rand.nextDouble() + 1, rand.nextDouble(), 0),
                                        size);            
        }     
 
        gameEntities.lootDrop(ObjectType.TYPE_ASTEROID, debrisLoc, 1/asteroid.radius);
 
        // And remove the old one       
        ed.removeEntity(asteroid.bodyId);
    }
}
