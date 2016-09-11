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

package example.view;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.scene.Node;

import com.simsilica.lemur.*;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputMapper.Mapping;
import com.simsilica.lemur.style.ElementId;

import com.simsilica.es.*;

import example.ConnectionState;
import example.Main;
import example.MainGameFunctions;
import example.MainMenuState;

/**
 *  Presents a help popup showing the list of current players.
 *
 *  @author    Paul Speed
 */
public class PlayerListState extends BaseAppState {

    private Container window;
    private boolean movementState = false;

    private ListBox<Entity> playerList;
    
    private EntityData ed;
    private EntitySet players;
    
    public PlayerListState() {
        setEnabled(false);
    }
 
    public void close() {
        setEnabled(false);
    }
    
    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }
        
    @Override 
    protected void initialize( Application app ) {
        
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate(MainGameFunctions.F_PLAYER_LIST, this, "toggleEnabled");
 
        window = new Container();
        Label title = window.addChild(new Label("Online Players", new ElementId("title"))); 
        //title.setFontSize(24);
        title.setInsets(new Insets3f(2, 2, 0, 2));
 
        playerList = window.addChild(new ListBox<Entity>());
        
        window.addChild(new ActionButton(new CallMethodAction("Done", this, "close")));
        
        this.ed = getState(ConnectionState.class).getEntityData();                
    }
    
    @Override 
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate(MainGameFunctions.F_PLAYER_LIST, this, "toggleEnabled");
    }
    
    @Override
    protected void onEnable() {
    
        // Grab the latest player entity set
        this.players = ed.getEntities(Name.class);
    
        // Setup the panel for display
        Node gui = ((Main)getApplication()).getGuiNode();
        
        int width = getApplication().getCamera().getWidth();
        int height = getApplication().getCamera().getHeight();
 
        // Base size and positioning off of 1.5x the 'standard scale' 
        float standardScale = getState(MainMenuState.class).getStandardScale(); 
        window.setLocalScale(1.5f * standardScale);
        
        Vector3f pref = window.getPreferredSize();
        pref.x = Math.max(pref.x, width * 0.25f);
        window.setPreferredSize(pref);
        
        pref = pref.mult(1.5f * standardScale);
        
        
        window.setLocalTranslation(width * 0.5f - pref.x * 0.5f,
                                   height * 0.5f + pref.y * 0.5f,
                                   100);
        
        gui.attachChild(window);
        GuiGlobals.getInstance().requestFocus(window);
 
        // Kind of sucks that this isn't more decoupled.               
        if( getState(PlayerMovementState.class) != null ) {
            // Save the enabled state of the PlayerMovementState so that we
            // can restore it if the menu is closed.
            this.movementState = getState(PlayerMovementState.class).isEnabled();        
            getState(PlayerMovementState.class).setEnabled(false);
        }
    }
    
    @Override
    protected void onDisable() {
        window.removeFromParent();
        
        if( getState(PlayerMovementState.class) != null ) {        
            getState(PlayerMovementState.class).setEnabled(movementState);
        }
        
        // Let the set go and clear it
        players.release();
        players = null;
    }    

    @Override
    public void update( float tpf ) {
        if( players.applyChanges() ) {
            playerList.getModel().clear();
            playerList.getModel().addAll(players);
        }        
    }
}
