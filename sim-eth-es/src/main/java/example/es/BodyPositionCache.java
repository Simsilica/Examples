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

import com.simsilica.es.EntityId;
import com.simsilica.mathd.trans.PositionTransition3f;
import com.simsilica.mathd.trans.TransitionBuffer;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 *  BodyPosition components hold a buffer that should be shared
 *  across all references to the same entity.  It's not your normal
 *  immutable component so special care must be taken.  
 *  This is really outside the normal scope of Zay-ES so we will kind
 *  of back into it by keeping a cache of the internal buffers.
 *  BodyPosition will check this cache when initialized.
 *
 *  @author    Paul Speed
 */
public class BodyPositionCache {
 
    private static BodyPositionCache instance = new BodyPositionCache();
 
    /**
     *  Keeps track of the weak references that are ready to remove
     *  from our map.
     */
    private ReferenceQueue<TransitionBuffer<PositionTransition3f>> refs = new ReferenceQueue<>();
 
    /**
     *  A map with weakly referenced values.  We'll clean out the values
     *  from the queue whenever a new get() is performed.  The theory
     *  is that garbage hanging around is less bad if no one is requesting
     *  it anyway.
     */
    private Map<EntityId, WeakReference<TransitionBuffer<PositionTransition3f>>> map = new HashMap<>();
 
    
    public static TransitionBuffer<PositionTransition3f> getBuffer( EntityId id, int size ) {
        return instance.get(id, size);
    }
 
    protected synchronized TransitionBuffer<PositionTransition3f> get( EntityId id, int size ) {
    
        // See if we've already got one
        WeakReference<TransitionBuffer<PositionTransition3f>> result = map.get(id);
        if( result == null || result.get() == null ) {
            // Need to create a new one
            TransitionBuffer<PositionTransition3f> buffer = PositionTransition3f.createBuffer(size);
            result = new WeakReference<>(buffer);
            map.put(id, result);    
        }
 
        // Clean out any dead references to keep our map from growing and growing
        Reference toRemove;
        while( (toRemove = refs.poll()) != null ) {
            map.values().remove(toRemove);
        }                    
    
        return result.get();
    } 
}
