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
import com.jme3.math.ColorRGBA;
import com.simsilica.event.EventBus;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.state.CompositeAppState;

/**
 *  The core state that manages the game session.  This has several
 *  child app states whose lifecycles are directly linked to this one.
 *
 *  @author    Paul Speed
 */
public class GameSessionState extends CompositeAppState {

    public GameSessionState() {
        // add normal states on the super-constructor
        super(new MessageState()); 
     
        // Add states that need to support enable/disable independent of
        // the outer state using addChild().
        addChild(new InGameMenuState(false), true);
    }
 
    public void disconnect() {
        // Remove ourselves
        getStateManager().detach(this);
    }
    
    @Override   
    protected void initialize( Application app ) {
        super.initialize(app);
        
        EventBus.publish(GameSessionEvent.sessionStarted, new GameSessionEvent());

        getState(MessageState.class).addMessage("> You have joined the game.", ColorRGBA.Yellow);

        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup(MainGameFunctions.IN_GAME);            
    }
    
    @Override   
    protected void cleanup( Application app ) {
        
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup(MainGameFunctions.IN_GAME);        
        
        EventBus.publish(GameSessionEvent.sessionEnded, new GameSessionEvent());
 
        // The below will fail because there is no message state anymore... so
        // it wouldn't show the message anyway.       
        // getState(MessageState.class).addMessage("> You have left the game.", ColorRGBA.Yellow);
                
        super.cleanup(app);
    }
}
