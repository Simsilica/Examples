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

package example.net.server;

import com.simsilica.ethereal.zone.ZoneManager;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;

import example.sim.SimpleBody;
import example.sim.PhysicsListener;
import example.sim.SimplePhysics;

/**
 *  A game system that registers a listener with the SimplePhysics
 *  system and then forwards those events to the SimEtheral zone manager,
 *  which in turn will package them up for the clients in an efficient way.
 *
 *  @author    Paul Speed
 */
public class ZoneNetworkSystem extends AbstractGameSystem {
    
    private ZoneManager zones;
    private PhysicsObserver physicsObserver = new PhysicsObserver();
    
    public ZoneNetworkSystem( ZoneManager zones ) {
        this.zones = zones;
    }
     
    @Override
    protected void initialize() {
        getSystem(SimplePhysics.class).addPhysicsListener(physicsObserver);
    }

    @Override
    protected void terminate() {
        getSystem(SimplePhysics.class).removePhysicsListener(physicsObserver);
    }
    
    /**
     *  Listens for changes in the physics objects and sends them
     *  to the zone manager.
     */
    private class PhysicsObserver implements PhysicsListener {
 
        @Override   
        public void beginFrame( SimTime time ) {
            zones.beginUpdate(time.getTime());
        }
 
        @Override   
        public void addBody( SimpleBody body ) {
            // Don't really care about this
        }
        
        @Override   
        public void updateBody( SimpleBody body ) {
            zones.updateEntity(body.bodyId.getId(), true, body.pos, body.orientation, body.bounds);   
        }
 
        @Override   
        public void removeBody( SimpleBody body ) {
            zones.remove(body.bodyId.getId());
        }
    
        @Override   
        public void endFrame( SimTime time ) {
            zones.endUpdate();
        } 
        
    }
}


