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

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.ethereal.TimeSource;


/**
 *  Provides a consistent frame time to the classes that want it.
 *  Time inevitably marches forward except in the case of this state.
 *  When update() is called, the frame time is locked such that getTime()
 *  will return the same value until after the next update().  This makes
 *  sure that small inter-frame time differences don't creep into interpolated
 *  visuals.
 *
 *  For a simple game example like this, it isn't really necessary but it's
 *  a good pattern to follow.
 *
 *  @author    Paul Speed
 */
public class TimeState extends BaseAppState {

    private TimeSource timeSource;
    private long frameTime;
    private long realTime;

    public TimeState() {
    }
    
    public TimeState( TimeSource timeSource ) {
        this.timeSource = timeSource;
    }
 
    public void setTimeSource( TimeSource timeSource ) {
        this.timeSource = timeSource;
    }
    
    public TimeSource getTimeSource() {
        return timeSource;
    }
    
    public long getTime() {
        return frameTime; 
    }
    
    public long getRealTime() {
        return realTime;
    }
    
    public void update( float tpf ) {
        if( timeSource != null ) {
            this.frameTime = timeSource.getTime();
            this.realTime = System.nanoTime();
        }
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
}
