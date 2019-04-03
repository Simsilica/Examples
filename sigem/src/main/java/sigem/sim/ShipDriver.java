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

import org.slf4j.*;

import com.jme3.math.*;

import com.simsilica.mathd.*;

import sigem.es.*;

/**
 *  Applies thrust values to a ship.
 *
 *  @author    Paul Speed
 */
public class ShipDriver implements ControlDriver {
 
    static Logger log = LoggerFactory.getLogger(ShipDriver.class);
 
    private Body body;
 
    // Keep track of what the player has provided.
    private volatile Vec3d thrust = new Vec3d();
 
    private Vec3d dir = new Vec3d();
    private Vec3d targetVel = new Vec3d();
    private Vec3d left = new Vec3d();
    //private float maxSpeed = 10;
    //private float turnSpeed = 1;   
    private float maxSpeed = 20;
    private float turnSpeed = 2;   
    private double lateralBraking = 0.9;
    
    public ShipDriver() {
    }
 
    public Body getBody() {
        return body;
    }
 
    public Vec3d getThrust() {
        return thrust;
    }
 
    public void applyControlInput( ShipInput input ) {
        this.thrust = input.getThrust();
    }
    
    @Override
    public void initialize( Body body ) {
        this.body = body;
    } 
 
    @Override
    public void update( double stepTime, Body body ) {
 
        // Grab local versions of the player settings in case another
        // thread sets them while we are calculating.
        Vec3d vec = thrust;
 
        // Add a threshold to thrust to make it easier to turn with the
        // gamepads without engaging forward thrust.               
        if( thrust.z > 0.2 ) {
            // Localize the direction vector
            body.orientation.mult(Vec3d.UNIT_Z, dir); 
            body.orientation.mult(Vec3d.UNIT_X, left); 
        
            // Calculate the target velocity from the current direction
            targetVel.set(dir).multLocal(thrust.z * maxSpeed);
 
            // We want to apply our acceleration to reach the target 
            // velocity... unless that acceleration would actually slow
            // us down (ie: we're heading towards a gravity well)
            // So we can't just subtract the tips of targetVel and velocity
            // and use that as acceleration.  It might slow us down.
            // But we _do_ want to apply any lateral acceleration.
            // So I think we need to break out velocity into local
            // components.
            double xTarget = left.dot(targetVel);
            double zTarget = dir.dot(targetVel);
            
            double xVel = left.dot(body.velocity);
            double zVel = dir.dot(body.velocity);
            
            double x = xTarget - xVel;
            double z = Math.max(0, zTarget - zVel);

            // Reconstruct the acceleration vector
            dir.multLocal(z);
            left.multLocal(x * lateralBraking);            
            body.acceleration.set(dir).addLocal(left);

            // Set the thrust acceleration
            //body.acceleration.set(targetVel.subtract(body.velocity));
        } else {
            // No acceleration due to thrust
            body.acceleration.set(0, 0, 0);
        }
        
        // Turning controls velocity directly
        // Thrust is negative to turn left... but angles are positive to turn
        // left... so we invert.
        body.rotVelocity.set(0, turnSpeed * -thrust.x, 0);
        
    }
}
