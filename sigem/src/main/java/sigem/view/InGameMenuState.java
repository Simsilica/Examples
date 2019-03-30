/*
 * $Id$
 * 
 * Copyright (c) 2019, Simsilica, LLC
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

package sigem.view;

import java.util.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.scene.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.ElementId;

import sigem.MainGameFunctions;
import sigem.MainMenuState;

/**
 *
 *
 *  @author    Paul Speed
 */
public class InGameMenuState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(InGameMenuState.class);

    private Container mainWindow;
    private TabbedPanel tabs;

    private List<Action> sessionActions = new ArrayList<>();
    private Container sessionButtons;

    private boolean movementState = false;

    public InGameMenuState( boolean enabled ) {
        setEnabled(enabled);
        sessionActions.add(new CallMethodAction("Resume", this, "resume"));         
        sessionActions.add(new CallMethodAction("Exit", this, "exitGame"));
    }

    /**
     *  Returns the tabbed panel used in the in-game menu.  This lets other
     *  states potentially add their own in-game menu tabs.
     */ 
    public TabbedPanel getTabs() {
        return tabs;
    }

    protected void resume() {
        setEnabled(false);
    }
    
    protected void stopGame() {
        getStateManager().detach(getState(GameSessionState.class));
    }
    
    protected void exitGame() {
        log.info("exitGame()");
        
log.info("test:" + getState(com.simsilica.lemur.event.PopupState.class).getGuiSize());        
        getState(OptionPanelState.class).show("Exit Game?", "Really exit and return\nto the main menu?", 
                                              new CallMethodAction("Yes", 
                                                            this, 
                                                            "stopGame"),
                                              new EmptyAction("No"),
                                              new EmptyAction("Cancel"));
    }
 
    public List<Action> getSessionActions() {
        return sessionActions;
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }
    
    @Override   
    protected void initialize( Application app ) {
        mainWindow = new Container();
         
        tabs = mainWindow.addChild(new TabbedPanel());
        
        sessionButtons = tabs.addTab("Game Session", new Container());
        for( Action a : getSessionActions() ) {
            sessionButtons.addChild(new ActionButton(a));
        }
           
        // Calculate a sposition from the UI's height
        float height = getState(MainMenuState.class).getUiSize().y;
        Vector3f pref = mainWindow.getPreferredSize().clone();
        
        // With a slight bias toward the top        
        float y = height * 0.6f + pref.y * 0.5f;
                                     
        mainWindow.setLocalTranslation(100, y, 0);
                
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate(MainGameFunctions.F_IN_GAME_MENU, this, "toggleEnabled");
    }
 
    @Override   
    protected void cleanup( Application app ) {   
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate(MainGameFunctions.F_IN_GAME_MENU, this, "toggleEnabled");
    }
    
    @Override   
    protected void onEnable() {
        getState(GameSessionState.class).pause();
        getState(MainMenuState.class).getUiNode().attachChild(mainWindow);
        GuiGlobals.getInstance().requestFocus(mainWindow);
        GuiGlobals.getInstance().requestCursorEnabled(mainWindow);
    }
    
    @Override   
    protected void onDisable() {
        mainWindow.removeFromParent();
        GuiGlobals.getInstance().releaseCursorEnabled(mainWindow);
        if( getState(GameSessionState.class) == null ) {
            // We're being detached from exiting the game
        } else {
            getState(GameSessionState.class).resume();
        }
    }
}
