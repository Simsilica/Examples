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

package sigem.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.mathd.Quatd;
import com.simsilica.mathd.Vec3d;


/**
 *  The position of static or mostly static objects.  This is the position
 *  for entities that move infrequently and is the initial position used
 *  for mobile objects.  A physics listener may also occasionally publish
 *  updates to the real position of mobile objects. 
 *
 *  @author    Paul Speed
 */
public class Position implements EntityComponent {
    private Vec3d location;
    private Quatd facing;
    
    // When we want to filter static objects based on position then
    // this will be very useful and we'll need to generate it whenever the
    // position changes.
    //private long cellId;
    
    public Position() {
        this(0, 0, 0);
    }
    
    public Position( double x, double y, double z ) {
        this(new Vec3d(x, y, z), new Quatd());
    }
    
    public Position( Vec3d loc ) {
        this(loc, new Quatd());
    }
    
    public Position( Vec3d loc, Quatd quat ) {
        this.location = loc;
        this.facing = quat;
    }
    
    public Position changeLocation( Vec3d location ) {
        return new Position(location, facing);
    }

    public Position changeFacing( Quatd facing ) {
        return new Position(location, facing);
    }
    
    public Vec3d getLocation() {
        return location;
    }
    
    public Quatd getFacing() {
        return facing;
    }
    
    @Override
    public String toString() {
        return "Position[location=" + location + ", facing=" + facing + "]";
    }
}
