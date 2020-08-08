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

import java.util.Objects;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.simsilica.es.EntityData;
import com.simsilica.sim.AbstractGameSystem;

import sigem.es.ObjectType;

/**
 *  Checks for any missile hits to create the hit effect and destroy
 *  the missile entity.  It's up to other listeners to deal with the damage.
 *
 *  @author    Paul Speed
 */
public class MissileHitListener extends AbstractGameSystem  
                                 implements ContactListener {
                                 
    static Logger log = LoggerFactory.getLogger(MissileHitListener.class);
 
    private EntityData ed;
    private GameEntities gameEntities;
    @SuppressWarnings("unused")
	private Random rand = new Random(0);
                                 
    public MissileHitListener() {
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
        
        if( c.energy == 0 ) {
            // An early listener already said this wasn't a real contact
            return;
        }
    
        ObjectType type1 = ed.getComponent(c.b1.bodyId, ObjectType.class);
        ObjectType type2 = ed.getComponent(c.b2.bodyId, ObjectType.class);
        
        // Entities may have already been removed
        String t1 = type1 == null ? null : type1.getTypeName(ed);
        String t2 = type2 == null ? null : type2.getTypeName(ed);  
 
        if( Objects.equals(t1, t2) ) {
            // Missiles or not, we don't care if they are the same... even
            // missiles can pass each other by.
            return;
        }
        
        if( ObjectType.TYPE_MISSILE.equals(t1) ) {
            // Blow up b2
            missileHit(c.b1, c);
        } else if( ObjectType.TYPE_MISSILE.equals(t2) ) {
            // Blow up b1
            missileHit(c.b2, c);
        } 
        
    }
    
    protected void missileHit( Body missile, Contact contact ) {
        ed.removeEntity(missile.bodyId);
        gameEntities.createExplosion(missile.pos, 1);           
    }
}
