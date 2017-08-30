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

import com.jme3.math.*;

import com.simsilica.mathd.*;
import example.PhysicsConstants;
import org.dyn4j.geometry.Vector2;

/**
 * Uses rotation and a 3-axis thrust vector to supply specific velocity to a
 * body. We ignore the normal physics acceleration for now and just set the
 * velocity directly based on our accelerated thrust values.
 *
 * @author Paul Speed
 */
public class ShipDriver implements ControlDriver {

    // Keep track of what the player has provided.
    private volatile Quaternion orientation = new Quaternion();
    private volatile Vector3f thrust = new Vector3f();

    private double pickup = 3;

    // The velocity in ship space, not world space    
    private Vec3d velocity = new Vec3d();

    public void applyMovementState(Vector3f thrust) {
        this.thrust = thrust;
    }

    private double applyThrust(double v, double thrust, double tpf) {
        if (thrust > 0) {
            // Accelerate
            v = Math.min(thrust, v + pickup * tpf);
        } else if (thrust < 0) {
            // Decelerate
            v = Math.max(thrust, v - pickup * tpf);
        } else {
            if (v > 0) {
                // Fall to zero
                v = Math.max(0, v - pickup * tpf);
            } else {
                // Rist to zero
                v = Math.min(0, v + pickup * tpf);
            }
        }
        return v;
    }

    @Override
    public void update(double stepTime, SimpleBody body) {

        // Grab local versions of the player settings in case another
        // thread sets them while we are calculating.
        Quaternion quat = orientation;
        Vector3f vec = thrust;

        velocity.x = applyThrust(velocity.x, vec.x, stepTime);
        velocity.y = applyThrust(velocity.y, vec.y, stepTime);
        //velocity.z = applyThrust(velocity.z, vec.z, stepTime); //Disabled z-thrust in order to stay in the x-y plane

        // Setup the current world rotation of the body 
        body.orientation.set(quat.getX(), quat.getY(), quat.getZ(), quat.getW());

        // Apply the accelerated velocity oriented into world space       
        body.velocity3d = body.orientation.mult(velocity, body.velocity3d);

        //Update physics
        //TODO: Should depend on the ship type that is being controlled
        Vector2 linVel = new Vector2(0, vec.y * PhysicsConstants.SHIPTHRUST);
        //Velocity since we want the ship to stop rotating right away if player releases the rotate keys        
        body.setAngularVelocity(vec.x);
        double rotation = body.getTransform().getRotation(); //Is the rotation from last frame
        linVel.rotate(rotation);
        body.applyForce(linVel);
    }
}
