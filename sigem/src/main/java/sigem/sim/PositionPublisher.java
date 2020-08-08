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

import com.simsilica.es.EntityData;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;

import sigem.es.Position;


/**
 *  Publishes physics body positions to a Position component.
 *
 *  @author    Paul Speed
 */
public class PositionPublisher extends AbstractGameSystem  
                               implements PhysicsListener {
 
    private EntityData ed;
    @SuppressWarnings("unused")
    private SimTime time;
    
    public PositionPublisher() { 
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
   
    @Override
    public void beginFrame( SimTime time ) {
        this.time = time;
    }
 
    @Override
    public void addBody( Body body ) {    
    }
    
    @Override
    public void updateBody( Body body ) {
        Position pos = new Position(body.pos, body.orientation);
        ed.setComponent(body.bodyId, pos);
    }
 
    @Override
    public void removeBody( Body body ) {
        ed.removeComponent(body.bodyId, Position.class);
    }

    @Override
    public void endFrame( SimTime time ) {
    }
}


