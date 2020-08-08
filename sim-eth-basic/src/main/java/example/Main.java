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

import com.jme3.app.BasicProfilerState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.simsilica.event.EventBus;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.state.DebugHudState;
import com.simsilica.util.LogAdapter;

import example.view.PlayerMovementFunctions;

/**
 *  The main bootstrap class for the SimEthereal networking example
 *  game. 
 *
 *  @author    Paul Speed
 */
public class Main extends SimpleApplication {

    @SuppressWarnings("unused")
    private Node logo;

    public static void main( String... args ) throws Exception {
        System.out.println("SimEthereal Example");

        // Make sure JUL logging goes to our log4j configuration
        LogAdapter.initialize();
        
        Main main = new Main();
        
        AppSettings settings = new AppSettings(true);
 
        // Set some defaults that will get overwritten if
        // there were previously saved settings from the last time the user
        // ran.       
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);
        
        settings.load("SimEthereal Example");
        settings.setTitle("SimEthereal Example");
        settings.setSettingsDialogImage("/sim-eth-basic-splash-512.png");
        settings.setUseJoysticks(true);
        /*
        try {
            BufferedImage[] icons = new BufferedImage[] {
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-128.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-32.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-16.png" ) )
                };
            settings.setIcons(icons);
        } catch( IOException e ) {
            log.warn( "Error loading globe icons", e );
        }*/        
        
        main.setSettings(settings);
        
        main.start();
    }

    public Main() {
        super(new StatsAppState(), new DebugKeysAppState(), new BasicProfilerState(false),
              new AnimationState(), // from Lemur
              new OptionPanelState(), // from Lemur
              new DebugHudState(), // SiO2 utility class
              new SiliconDioxideState(),
              new MainMenuState(),
              //new FlyCamAppState(), // temporary
              new ScreenshotAppState("", System.currentTimeMillis())); 
    }
        
    public void simpleInitApp() {        
        
        setPauseOnLostFocus(false);
        setDisplayFps(false);
        setDisplayStatView(false);
        
        GuiGlobals.initialize(this);
 
        GuiGlobals globals = GuiGlobals.getInstance();
        BaseStyles.loadGlassStyle();
        globals.getStyles().setDefaultStyle("glass");
 
        MainGameFunctions.initializeDefaultMappings(globals.getInputMapper());
        PlayerMovementFunctions.initializeDefaultMappings(globals.getInputMapper());
 
        // Since we've added the background spinning widget here, we'll        
        // also register events to enable/disable it.
        EventBus.addListener(new GameListener(), 
                             GameSessionEvent.sessionStarted,
                             GameSessionEvent.sessionEnded);
 
        // Get rid of the default close mapping in InputManager
        if( inputManager.hasMapping(INPUT_MAPPING_EXIT) ) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }
 
        /*
        Spatial test = assetManager.loadModel("Models/fighter.j3o");
        Texture texture = assetManager.loadTexture("Textures/ship1.png");
        Material mat = globals.createMaterial(texture, false).getMaterial();
        test.setMaterial(mat);
        rootNode.attachChild(test);*/
                       
    }
    
    private class GameListener {
        @SuppressWarnings("unused")
        public void sessionStarted( GameSessionEvent event ) {
            stateManager.getState(SiliconDioxideState.class).setEnabled(false);
        }
        
        @SuppressWarnings("unused")
        public void sessionEnded( GameSessionEvent event ) {
            stateManager.getState(SiliconDioxideState.class).setEnabled(true);
        }
    }
}


