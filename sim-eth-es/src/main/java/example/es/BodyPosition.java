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
package example.es;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
import com.simsilica.mathd.trans.PositionTransition;
import com.simsilica.mathd.trans.TransitionBuffer;

/**
 * A component representing the position of mobile objects that internally keeps
 * track of a small history of position, rotation, and visibility changes. This
 * is not a normal immutable component and thus manages its own threading.
 * Furthermore, special care is taken to make sure that all BodyPosition objecst
 * for a particular entity share the internal data buffer.
 *
 * @author Paul Speed
 */
public final class BodyPosition implements EntityComponent {

    private transient int size;
    private transient TransitionBuffer<PositionTransition> position;

    public BodyPosition() {
    }

    public BodyPosition(int history) {
        this.size = (byte) history;
        this.position = PositionTransition.createBuffer(history);
    }

    /**
     * Called for a retrieved entity to make sure this BodyPosition has it's
     * shared transition buffer. It must be called for all retrieved
     * BodyPosition components before use.
     */
    public void initialize(EntityId id, int size) {
        if (this.position == null) {
            this.size = size;
            this.position = BodyPositionCache.getBuffer(id, size);
        }
    }

    public TransitionBuffer<PositionTransition> getBuffer() {
        return position;
    }

    public void addFrame(long endTime, Vector3f pos, Quaternion quat, boolean visible) {
        PositionTransition trans = new PositionTransition(endTime, pos, quat, visible);
        getBuffer().addTransition(trans);
    }

    public PositionTransition getFrame(long time) {
        return getBuffer().getTransition(time);
    }

    @Override
    public String toString() {
        return "BodyPosition[" + position + "]";
    }
}
