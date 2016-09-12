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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyNames;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import com.simsilica.lemur.*;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputMapper.Mapping;
import com.simsilica.lemur.style.ElementId;

import example.view.PlayerMovementFunctions;
import example.view.PlayerMovementState;

/**
 *  Presents a help popup to the user when they press F1.
 *
 *  @author    Paul Speed
 */
public class HelpState extends BaseAppState {

    private Container helpWindow;
    private boolean movementState = false;
    
    private KeyHelp[] keyHelp = {
        new KeyHelp(MainGameFunctions.F_IN_GAME_HELP, "Opens this help window."),
        new KeyHelp(PlayerMovementFunctions.F_X_ROTATE, "Rotates left/right."),
        new KeyHelp(PlayerMovementFunctions.F_Y_ROTATE, "Rotates up/down."),
        new KeyHelp(PlayerMovementFunctions.F_THRUST, "Flies forward and back."),
        new KeyHelp(PlayerMovementFunctions.F_STRAFE, "Flies side to side."),
        new KeyHelp(PlayerMovementFunctions.F_ELEVATE, "Flies up or down."),
        new KeyHelp(MainGameFunctions.F_COMMAND_CONSOLE, 
            "Opens the in-game chat bar.  Type chat messages",
            "and hit enter to send.",
            "Hit enter or esc to close."),
        new KeyHelp(MainGameFunctions.F_IN_GAME_MENU, "Opens the in-game menu."),
        new KeyHelp(MainGameFunctions.F_PLAYER_LIST, "Displays the list of online players."),
        new KeyHelp("PrtScrn", "Takes a screen shot."),
        new KeyHelp("F5", "Toggles display stats."),
        new KeyHelp("F6", "Toggles rendering frame timings."),
        new KeyHelp(MainGameFunctions.F_TIME_DEBUG, "Toggles network timing stats.") 
    };

    public HelpState() {
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
        inputMapper.addDelegate(MainGameFunctions.F_IN_GAME_HELP, this, "toggleEnabled");
 
        helpWindow = new Container();
        Label title = helpWindow.addChild(new Label("In-Game Help", new ElementId("title"))); 
        //title.setFontSize(24);
        title.setInsets(new Insets3f(2, 2, 0, 2));
 
        Container sub = helpWindow.addChild(new Container());
        sub.setInsets(new Insets3f(10, 10, 10, 10));
        sub.addChild(new Label("Key Bindings")); 

        Container keys = sub.addChild(new Container());
 
        Joiner commas = Joiner.on(", ");
        Joiner lines = Joiner.on("\n"); 
        for( KeyHelp help : keyHelp ) {
            help.updateKeys(inputMapper);
            String s = commas.join(help.keyNames);
            keys.addChild(new Label(s, new ElementId("help.key.label")));
            s = lines.join(help.description);
            keys.addChild(new Label(s, new ElementId("help.description.label")), 1);                     
        }       

        helpWindow.addChild(new ActionButton(new CallMethodAction("Done", this, "close")));
                
        System.out.println("All InputMapper function mappings:");       
        for( FunctionId id : inputMapper.getFunctionIds() ) {
            System.out.println(id);
            System.out.println("  mappings:");
            for( Mapping m : inputMapper.getMappings(id) ) {
                System.out.println("    " + m);
                Object o = m.getPrimaryActivator();
                if( o instanceof Integer ) {
                    Integer keyCode = (Integer)o;
                    System.out.println("      primary:" + KeyNames.getName(keyCode));                    
                } else {
                    System.out.println("      primary:" + o);
                }
                for( Object mod : m.getModifiers() ) {
                    if( mod instanceof Integer ) {
                        Integer keyCode = (Integer)mod;
                        System.out.println("      modifier:" + KeyNames.getName(keyCode));                    
                    }
                }
            }
        }
    }
    
    @Override 
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate(MainGameFunctions.F_IN_GAME_HELP, this, "toggleEnabled");
    }
    
    @Override
    protected void onEnable() {
        Node gui = ((Main)getApplication()).getGuiNode();
        
        int width = getApplication().getCamera().getWidth();
        int height = getApplication().getCamera().getHeight();
 
        // Base size and positioning off of 1.5x the 'standard scale' 
        float standardScale = getState(MainMenuState.class).getStandardScale(); 
        helpWindow.setLocalScale(1.5f * standardScale);
        
        Vector3f pref = helpWindow.getPreferredSize();
        pref.multLocal(1.5f * standardScale);
        
        helpWindow.setLocalTranslation(width * 0.5f - pref.x * 0.5f,
                                       height * 0.5f + pref.y * 0.5f,
                                       100);
        
        gui.attachChild(helpWindow);
        GuiGlobals.getInstance().requestFocus(helpWindow);
 
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
        helpWindow.removeFromParent();
        
        if( getState(PlayerMovementState.class) != null ) {        
            getState(PlayerMovementState.class).setEnabled(movementState);
        }
    }
    
    private class KeyHelp {
        FunctionId function;
        String[] keyNames;
        String[] description;
        
        public KeyHelp( FunctionId function, String... description ) {
            this.function = function;
            this.description = description;
        }
        
        public KeyHelp( String keys, String... description ) {
            this.keyNames = new String[] { keys };
            this.description = description;
        } 
        
        public void updateKeys( InputMapper inputMapper ) {
            if( function == null ) {
                return;
            }
            
            List<String> names = new ArrayList<>();
            
            for( Mapping m : inputMapper.getMappings(function) ) {
                Object o = m.getPrimaryActivator();
                if( !(o instanceof Integer) ) {
                    // Not a key mapping
                    continue;
                }
                                
                Integer primary = (Integer)o;
                
                StringBuilder sb = new StringBuilder(KeyNames.getName(primary));
                for( Object mod : m.getModifiers() ) {
                    if( mod instanceof Integer ) {
                        sb.append("+");
                        sb.append(KeyNames.getName((Integer)mod));
                    }
                }
                names.add(sb.toString());               
            }
            keyNames = new String[names.size()];
            keyNames = names.toArray(keyNames); 
        }
    }
}
