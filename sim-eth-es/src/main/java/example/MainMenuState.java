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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
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
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.ElementId;

/**
 *
 *
 *  @author    Paul Speed
 */
public class MainMenuState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(MainMenuState.class);

    private Container mainWindow;
    
    // For joining a server
    private TextField connectHost;
    private TextField connectPort;

    // For hosting a server
    private TextField hostPort;
    private TextField hostDescription;

    public MainMenuState() {
    }
    
    public float getStandardScale() {
        int height = getApplication().getCamera().getHeight();        
        return height / 720f;
    }
    
    protected void showError( String title, String error ) {
        getState(OptionPanelState.class).show(title, error);    
    }
 
    protected int parseInt( String title, String sInt ) {
        sInt = sInt.trim();
        if( sInt.length() == 0 ) {
            showError(title + " Error", "Please specify a port.\nDefault is " + GameConstants.DEFAULT_PORT);
            return -1;
        }
        try {
            return Integer.parseInt(sInt);
        } catch( Exception e ) {
            log.error("Error parsing port:" + sInt, e);
            showError(title + " Error", "Invalid port:" + sInt + "\n" 
                      + e.getClass().getSimpleName() + ":" + e.getMessage());
            return -1;
        }   
    }
 
    protected void connect() {
        log.info("Connect... host:" + connectHost.getText() + "  port:" + connectPort.getText());

        // Validate the parameters        
        String host = connectHost.getText().trim();
        if( host.length() == 0 ) {
            showError("Connect Error", "Please specify a host.");
            return;
        }
        int port = parseInt("Connect", connectPort.getText());        

        // Add the state that will manage our remote connection and create
        // the game states and so on.
        getStateManager().attach(new ConnectionState(this, host, port));
        
        // Disable ourselves
        setEnabled(false);
    }
    
    protected void host() {
        log.info("Host a game on port:" + hostPort.getText());
        log.info("Description:");
        log.info(hostDescription.getText());
        
        // Validate the parameters
        int port = parseInt("Hosting", hostPort.getText());

        try {
            // Add the state to manage the hosting environment.  It will launch
            // a self-connecting ConnectionState on its own.        
            getStateManager().attach(new HostState(port, hostDescription.getText()));
        
            // Disable ourselves
            setEnabled(false);
        } catch( RuntimeException e ) {
            log.error("Error attaching host state", e);
            String message = "Error hosting game on port:" + port;
            Throwable cause = e.getCause();
            if( cause != null ) {
                message += "\n" + cause.getClass().getSimpleName() + ":" + cause.getMessage();
            }
            showError("Hosting", message);             
        }
    }
    
    @Override   
    protected void initialize( Application app ) {
        mainWindow = new Container();
 
        Label title = mainWindow.addChild(new Label("SimEthereal Example 2"));
        title.setFontSize(32);
        title.setInsets(new Insets3f(10, 10, 0, 10));
        Label subtitle = mainWindow.addChild(new Label("with Zay-ES"));
        subtitle.setInsets(new Insets3f(0, 10, 0, 10));
        subtitle.setTextHAlignment(HAlignment.Right);
        subtitle.setColor(new ColorRGBA(166/255f, 107/255f, 255/255f, 0.85f));        
        
        Container props;
        
        Container joinPanel = mainWindow.addChild(new Container());
        joinPanel.setInsets(new Insets3f(10, 10, 10, 10));
        joinPanel.addChild(new Label("Join a Network Server", new ElementId("title")));
        
        props = joinPanel.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Last)));
        props.setBackground(null);
        props.addChild(new Label("Connect to host:"));
        connectHost = props.addChild(new TextField("localhost"), 1);
        props.addChild(new Label("On port:"));
        connectPort = props.addChild(new TextField(String.valueOf(GameConstants.DEFAULT_PORT)), 1);
        joinPanel.addChild(new ActionButton(new CallMethodAction("Connect", this, "connect")));
 
        Container hostPanel = mainWindow.addChild(new Container());
        hostPanel.setInsets(new Insets3f(10, 10, 10, 10));
        hostPanel.addChild(new Label("Host a Game Server", new ElementId("title")));
                
        props = hostPanel.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Last)));
        props.setBackground(null);
        props.addChild(new Label("Host on port:"));
        hostPort = props.addChild(new TextField(String.valueOf(GameConstants.DEFAULT_PORT)), 1);
        
        hostPanel.addChild(new Label("Server Description"));
        hostDescription = hostPanel.addChild(new TextField("This a game server.\nThere are many like it\nbut this one is mine."));
        hostDescription.setSingleLine(false);
        hostPanel.addChild(new ActionButton(new CallMethodAction("Begin Hosting", this, "host")));
 
 
        ActionButton exit = mainWindow.addChild(new ActionButton(new CallMethodAction("Exit Game", app, "stop")));
        exit.setInsets(new Insets3f(10, 10, 10, 10)); 
           
        // Calculate a standard scale and position from the app's camera
        // height
        int height = app.getCamera().getHeight();        
        Vector3f pref = mainWindow.getPreferredSize().clone();
        
        float standardScale = getStandardScale();
        pref.multLocal(1.5f * standardScale);
 
        // With a slight bias toward the top        
        float y = height * 0.6f + pref.y * 0.5f;
                                     
        mainWindow.setLocalTranslation(100 * standardScale, y, 0);
        mainWindow.setLocalScale(1.5f * standardScale);
    }
 
    @Override   
    protected void cleanup( Application app ) {
    }
    
    @Override   
    protected void onEnable() {
        Node gui = ((Main)getApplication()).getGuiNode();
        gui.attachChild(mainWindow);
        GuiGlobals.getInstance().requestFocus(mainWindow);
    }
    
    @Override   
    protected void onDisable() {
        mainWindow.removeFromParent();
    }
}
