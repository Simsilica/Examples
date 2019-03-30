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

package sigem;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.scene.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;

import sigem.view.*;

/**
 *
 *
 *  @author    Paul Speed
 */
public class MainMenuState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(MainMenuState.class);

    private Node uiNode;
    private Vector2f uiSize;

    private Container mainWindow;
    
    public MainMenuState() {
    }
 
    public Node getUiNode() {
        return uiNode;
    }
    
    public Vector2f getUiSize() {
        return uiSize;
    }
    
    public float getUiScale() {
        int height = getApplication().getCamera().getHeight();        
        return (height / 720f) * 1.5f;
    }
 
    protected void play() {
        log.info("Play game!");
        getStateManager().attach(new GameSessionState());
    }
    
    public void showError( String title, String error ) {
        getState(OptionPanelState.class).show(title, error);    
    }
 
    @Override   
    protected void initialize( Application app ) {
        
        // A root uiNode that will contain all of the UI at a common scale
        uiNode = new Node("scaledGui");
        uiNode.setLocalScale(getUiScale());
        uiSize = new Vector2f(app.getCamera().getWidth(), app.getCamera().getHeight());
        uiSize.multLocal(1/getUiScale()); 

        // Override the PopupState guiNode so that popups will have a consistent look
        // to the rest of the UI
        getState(PopupState.class).setGuiNode(uiNode);
        
        // It will always be attached as long as the main menu state is attached.    
        Node gui = ((Main)getApplication()).getGuiNode();
        gui.attachChild(uiNode);
    
        mainWindow = new Container();
 
        Label title = mainWindow.addChild(new Label("Silicon Gemini"));
        title.setFontSize(32);
        title.setInsets(new Insets3f(10, 10, 0, 10));

        
        ActionButton start = mainWindow.addChild(new ActionButton(new CallMethodAction("Play Game", this, "play")));
        start.setInsets(new Insets3f(10, 10, 0, 10)); 

        ActionButton exit = mainWindow.addChild(new ActionButton(new CallMethodAction("Exit Game", app, "stop")));
        exit.setInsets(new Insets3f(10, 10, 10, 10)); 

        // Calculate a standard scale and position from the scaled UI height
        Vector3f pref = mainWindow.getPreferredSize().clone();
 
        // With a slight bias toward the top        
        float y = uiSize.y * 0.6f + pref.y * 0.5f;
                                     
        mainWindow.setLocalTranslation(100, y, 0);           
    }
 
    @Override   
    protected void cleanup( Application app ) {
        uiNode.removeFromParent();
    }
    
    @Override   
    protected void onEnable() {       
        uiNode.attachChild(mainWindow);
        GuiGlobals.getInstance().requestFocus(mainWindow);
        GuiGlobals.getInstance().requestCursorEnabled(mainWindow);
    }
    
    @Override   
    protected void onDisable() {
        GuiGlobals.getInstance().releaseCursorEnabled(mainWindow);
        mainWindow.removeFromParent();
    }
}
