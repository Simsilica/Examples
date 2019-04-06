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
import com.simsilica.es.EntityData;


/**
 *  For visible game objects, this is the type of object.
 *
 *  @author    Paul Speed
 */
public class ObjectType implements EntityComponent {

    public static final String TYPE_SHIP = "ship";
    public static final String TYPE_PLANET = "planet";
    public static final String TYPE_ASTEROID = "asteroid";
    public static final String TYPE_ASTEROID_CHUNK = "asteroidChunk";
    public static final String TYPE_THRUST = "thrust";
    public static final String TYPE_MISSILE = "missile";
    public static final String TYPE_PLASMA_EXPLOSION = "plasmaExplosion";
    public static final String TYPE_FUEL_DROP = "fuelDrop";
    public static final String TYPE_SHIELD_DROP = "shieldDrop";
            
    private int type;
 
    protected ObjectType() {
    }
    
    public ObjectType( int type ) {
        this.type = type;
    }
    
    public static ObjectType create( String typeName, EntityData ed ) {
        return new ObjectType(ed.getStrings().getStringId(typeName, true));
    }
    
    public int getType() {
        return type;
    }
    
    public String getTypeName( EntityData ed ) {
        return ed.getStrings().getString(type);                 
    }
 
    @Override   
    public String toString() {
        return getClass().getSimpleName() + "[type=" + type + "]";
    }     
}
