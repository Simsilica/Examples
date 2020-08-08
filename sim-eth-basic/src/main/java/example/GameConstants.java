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

package example;

import com.simsilica.ethereal.net.ObjectStateProtocol;
import com.simsilica.ethereal.zone.ZoneGrid;
import com.simsilica.mathd.Vec3i;
import com.simsilica.mathd.bits.QuatBits;
import com.simsilica.mathd.bits.Vec3Bits;


/**
 *
 *
 *  @author    Paul Speed
 */
public class GameConstants {

    public static final String GAME_NAME = "SimEtheral Example";
    public static final int PROTOCOL_VERSION = 43;
    public static final int DEFAULT_PORT = 4269;
 
    /**
     *  We add an extra channel on the client->server connection to send
     *  chat related messages.  This is its own separate TCP socket that
     *  avoids tying up the main connection.
     */
    public static final int CHAT_CHANNEL = 0;
        
    public static final int GRID_CELL_SIZE = 32;
    
    // To allow players to see farther in space, we'll use a larger grid
    // size for the zone manager.  We could have also used a wider zone radius
    // and we might use both.  The gridSize used in a real game is mostly a
    // balance between how likely it is that an object will fall into more than
    // one zone at a time, with how many objects are likely to be in a zone.
    // Also, while the zone radius can be increased to include more surrounding
    // zones in a player's view, there is considerably more management involved
    // with each new zone, more network messages, etc..  Finding the sweet spot
    // will depend largely on the game.
    private static final int gridSize = 64;
    
    /**
     *  The 3D zone grid definition that defines how space is broken
     *  up into network zones.  
     */
    public static final ZoneGrid ZONE_GRID = new ZoneGrid(gridSize, gridSize, gridSize);
 
    public static final float MAX_OBJECT_RADIUS = 5;
    
    /**
     *  Defines how many network message bits to encode the elements of position 
     *  fields.  This will be a function of the grid size and resolution desired.  Keep in
     *  mind that objects can be in a zone even if their raw position is not in that
     *  zone because their radius may overlap that zone.  So the proper range needs
     *  to account for this overlap or there will be odd position clipping at the
     *  borders as objects cross zone boundaries.
     */   
    public static final Vec3Bits POSITION_BITS = new Vec3Bits(-MAX_OBJECT_RADIUS, 
                                                              gridSize + MAX_OBJECT_RADIUS,
                                                              16);
 
    /** 
     *  Defines how many network message bits to encode the elements of rotation
     *  fields.  Given that rotation Quaternion values are always between -1 and 1,
     *  12 bits seems sufficient based on ultimate resolution and visual testing.  
     */
    public static final QuatBits ROTATION_BITS = new QuatBits(12);
 
    /**
     *  Defines the overall object protocol parameters for how many bits ar used
     *  to encode the various parts of an object update message.  
     *
     *  <p>The first parameter defines how many bits are used to encode zone IDs.  
     *  Zones are always defined relative to the player so this will be a function of
     *  the zone radius.  For example, a radius of (1, 1, 1) means a 3D grid that's
     *  3x3x3 or 27 different zones.  At least 5 bits would be necessary to encode
     *  those IDs.  I use 8 here arbitrarily to give some space for zone radius
     *  experimentation.  8 bits should support a zone radius up to (3, 3, 2).</p>
     *
     *  <p>The second parameter is how many bits are associated with the real
     *  object IDs.  These IDs are not sent with every message so it's important that
     *  the value properly encompass all potential IDs.  For example, using a long ID
     *  from an ES you would need 64 bits.  If game sessions are short lived and your
     *  object IDs are only ever 'int' then 32 bits is the proper value.  This should
     *  usually be 64 bits and I'm keeping it as such... even though this example
     *  could get away with 32 bits.  Since these values are not sent with every message
     *  and people might cut/paste these settings, I'm going with the safer choice.</p>
     *
     *  <p>The last two parameters are the Vec3 and Quat bit sizes defined above.</p>   
     */   
    public static final ObjectStateProtocol OBJECT_PROTOCOL 
                = new ObjectStateProtocol(8, 64, POSITION_BITS, ROTATION_BITS);
 
    /**
     *  Defines the 3D zone radius around which updates will be sent.  The player
     *  is always considered to be in the 'center' and this radius defines how
     *  many zones in each direction are included in the view.  (A 2D game might
     *  only define x,y and leave z as 0.)  So a radius of 1 in x means that the
     *  player can see one zone to either side of their current zone.
     *  A total zone radius of (1, 1, 1) means the player can see a total of 27
     *  zones including the zone they are in.
     */           
    public static final Vec3i ZONE_RADIUS = new Vec3i(1, 1, 1);
}



