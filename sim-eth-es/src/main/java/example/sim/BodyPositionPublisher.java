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

import com.simsilica.es.*;
import com.simsilica.mathd.*;
import com.simsilica.sim.*;

import example.es.BodyPosition;


/**
 *  Publishes to a BodyPosition component so that server-side systems
 *  have easy access to the mobile entity positions.  Since we wrote
 *  our own simple physics engine for this example, we could have just
 *  added BodyPosition as a field to the Body class but I wanted to show
 *  how one might integrate this component using a physics system that
 *  wouldn't let you do that.
 *
 *  Note: also adding the BodyPosition to the entity on the server
 *  is what makes it available on the client so that it can have a place
 *  to add its object update events from the network.  The BodyPosition
 *  component itself is actually transferred empty.
 *
 *  @author    Paul Speed
 */
public class BodyPositionPublisher extends AbstractGameSystem  
                                   implements PhysicsListener {
 
    private EntityData ed;
    private SimTime time;
    
    public BodyPositionPublisher() { 
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
    
        // The server side needs hardly any backlog.  We'll use 3 just in case
        // but 2 (even possibly 1) should be fine.  If we ever need to rewind
        // for shot resolution then we can increase the backlog as necessary
        BodyPosition bPos = new BodyPosition(3);
        
        // Note: we could have also initialized the body position here but
        // we've already done it in SimplePhysics's EntityContainer.
        ed.setComponent(body.bodyId, bPos);        
    }
    
    @Override
    public void updateBody( Body body ) {
        BodyPosition pos = ed.getComponent(body.bodyId, BodyPosition.class);
        pos.addFrame(time.getTime(), body.pos.toVector3f(), body.orientation.toQuaternion(), true);
    }
 
    @Override
    public void removeBody( Body body ) {
        // Give one last position update with the visibility shut off
        BodyPosition pos = ed.getComponent(body.bodyId, BodyPosition.class);
        pos.addFrame(time.getTime(), body.pos.toVector3f(), body.orientation.toQuaternion(), false);
    }

    @Override
    public void endFrame( SimTime time ) {
    }
}


