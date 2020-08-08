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

import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.Name;
import com.simsilica.mathd.Quatd;
import com.simsilica.mathd.Vec3d;

import example.es.MassProperties;
import example.es.ObjectTypes;
import example.es.Position;
import example.es.SphereShape;

/**
 *  Utility methods for creating the common game entities used by 
 *  the simulation.  In cases where a game entity may have multiple
 *  specific componnets or dependencies used to create it, it can be
 *  more convenient to have a centralized factory method.  Especially
 *  if those objects are widely used.  For entities with only a few
 *  components or that are created by one system and only consumed by
 *  one other, then this is not necessarily true.
 *
 *  @author    Paul Speed
 */
public class GameEntities {

    public static EntityId createShip( EntityId parent, EntityData ed ) {
        EntityId result = ed.createEntity();
        Name name = ed.getComponent(parent, Name.class);
        ed.setComponent(result, name);
        ed.setComponents(result, ObjectTypes.shipType(ed),
                         new MassProperties(1/50.0), new SphereShape(3, new Vec3d()));
        
        return result;
    }
    
    public static EntityId createGravSphere( Vec3d pos, double radius, EntityData ed ) {
        EntityId result = ed.createEntity();
        ed.setComponents(result, ObjectTypes.gravSphereType(ed), 
                         new Position(pos, new Quatd().fromAngles(-Math.PI * 0.5, 0, 0)), 
                         new SphereShape(radius, new Vec3d()));
        return result;         
    }
}
