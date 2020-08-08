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

import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;

/**
 *  Cheating a little... we'll use a physics listener to add 
 *  gravity forces for the planet.
 *
 *  @author    Paul Speed
 */
public class PlanetGravity extends AbstractGameSystem  
                           implements PhysicsListener {

    static Logger log = LoggerFactory.getLogger(PlanetGravity.class);

    private Vec3d gravityWell = new Vec3d();
    private Vec3d force = new Vec3d();

    // How much of the gravity force is actually applied... we don't
    // want ships to orbit forever but break free on anything but a
    // direct approach.  We can tweak how strong that affect is here.
    // We apply forces asymmetrically, basically.  More when approaching,
    // less when leaving.
    private double closingFactor = 3; //1.75;
    private double leavingFactor = 0.5;
    // Note: this is not really the solution... but it works for now.
    // In Star Control you could actually orbit the planets and this
    // makes that impossible.  I think in Star Control it's probably that
    // max thrust is effectively higher when flying towards a gravity well.
    // ie: the engines are allowed to continue adding acceleration even
    // above a max speed.  We'd need to keep track of the external forces
    // separately and subtract them from the current velocity to clamp
    // speed.  Problem for another day.
    
    // Beyond this distance, gravity has no effect.
    //private double gravityThreshold = 100;
    private double gravityThreshold = 16 * 4;

    public PlanetGravity() { 
    }
 
    @Override
    protected void initialize() {
        getSystem(SimplePhysics.class).addPhysicsListener(this);
    }

    @Override
    protected void terminate() {
        getSystem(SimplePhysics.class).removePhysicsListener(this);
    }

    @Override
    public void beginFrame( SimTime time ) {
    }
 
    @Override
    public void addBody( Body body ) {
    }
    
    @Override
    public void updateBody( Body body ) {
    
        if( body.invMass == 0 ) {
            return;
        }
    
        // Apply gravity force based on current position
        
        // Calculate the force vector... points towards the
        // planet.
        force = force.set(gravityWell).subtractLocal(body.pos);
        double distanceSq = force.lengthSq();
        if( distanceSq < 0.001 || distanceSq > gravityThreshold * gravityThreshold ) {
            return;
        }

        // Are we heading toward the well or away?
        double closing = body.velocity.dot(force);
        
        // Fall-off 1/distance squared
        force.divideLocal(distanceSq);
        
        if( closing > 0 ) {
            // We're heading towards it
            force.multLocal(closingFactor);
        } else {
            // heading away
            force.multLocal(leavingFactor);
        }
        body.velocity.addLocal(force);
    }
 
    @Override
    public void removeBody( Body body ) {
    }
    
    @Override
    public void endFrame( SimTime time ) {
    }     
}


