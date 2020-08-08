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
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.ElementId;

/**
 *  A basic "login" state that provides a simple UI for logging in
 *  once a connection has been established.  This just manages the UI
 *  and calls back to the ConnectionState to do the actual 'work'.
 *
 *  @author    Paul Speed
 */
public class LoginState extends BaseAppState {

    private Container loginPanel;
    private TextField nameField;

    private Container serverInfoPanel;
    private String serverInfo;

    public LoginState( String serverInfo ) {
        this.serverInfo = serverInfo;
    }
 
    protected void join() {
        @SuppressWarnings("unused")
        String name = nameField.getText().trim();
        if( getState(ConnectionState.class).join(nameField.getText()) ) {
            getStateManager().detach(this);
        }
    } 
        
    protected void cancel() {
        getState(ConnectionState.class).disconnect();
        getStateManager().detach(this);
    }
    
    @Override   
    protected void initialize( Application app ) {
        loginPanel = new Container();
        loginPanel.addChild(new Label("Login", new ElementId("title")));
        
        Container props = loginPanel.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Last)));
        props.setBackground(null);        
        props.addChild(new Label("Name:"));
        nameField = props.addChild(new TextField(System.getProperty("user.name")), 1);
        
        Container buttons = loginPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)));
        buttons.setBackground(null);
        buttons.setLayout(new SpringGridLayout(Axis.X, Axis.Y));
        buttons.addChild(new ActionButton(new CallMethodAction("Join", this, "join"))); 
        buttons.addChild(new ActionButton(new CallMethodAction("Cancel", this, "cancel")));
        
        float scale = 1.5f * getState(MainMenuState.class).getStandardScale();
        loginPanel.setLocalScale(scale);
        
        Vector3f prefs = loginPanel.getPreferredSize().clone();
        prefs.x = Math.max(300, prefs.x);
        loginPanel.setPreferredSize(prefs.clone());
        
        // Now account for scaling
        prefs.multLocal(scale);
        
        int width = app.getCamera().getWidth();
        int height = app.getCamera().getHeight();
        
        loginPanel.setLocalTranslation(width * 0.5f - prefs.x * 0.5f, height * 0.5f + prefs.y * 0.5f, 10);
        
        serverInfoPanel = new Container();
        serverInfoPanel.setLocalScale(scale);
        serverInfoPanel.addChild(new Label("Server Description", new ElementId("title")));
        Label desc = serverInfoPanel.addChild(new Label(serverInfo));
        desc.setInsets(new Insets3f(5, 15, 5, 15)); // should leave this up to the style really
        desc.setTextHAlignment(HAlignment.Center);
        
        Vector3f prefs2 = serverInfoPanel.getPreferredSize().mult(scale);
        serverInfoPanel.setLocalTranslation(width * 0.5f - prefs2.x * 0.5f, 
                                            loginPanel.getLocalTranslation().y - prefs.y - 20 * scale,
                                            10);
        
    }
 
    @Override   
    protected void cleanup( Application app ) {
    }
    
    @Override   
    protected void onEnable() {
        Node root = ((Main)getApplication()).getGuiNode();
        root.attachChild(loginPanel);
        root.attachChild(serverInfoPanel);
        GuiGlobals.getInstance().requestFocus(loginPanel);
    }
 
    @Override   
    protected void onDisable() {
        loginPanel.removeFromParent();
        serverInfoPanel.removeFromParent();
    }
}
