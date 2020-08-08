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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.jme3.scene.Node;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.EmptyAction;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.state.DebugHudState;
import com.simsilica.state.DebugHudState.Location;

import example.net.server.GameServer;

/**
 *  Manages the game server when hosting a game.
 *
 *  @author    Paul Speed
 */
public class HostState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(HostState.class);

    private GameServer gameServer;
    private int port;

    private VersionedHolder<String> hostingState;
    private VersionedHolder<String> connectionCount;
    
    private ConnectionListener connectionListener = new ConnectionObserver();
    
    private Container hostWindow;
    
    public HostState( int port, String description ) {
        try {
            this.port = port;
            this.gameServer = new GameServer(port, description);
            gameServer.getServer().addConnectionListener(connectionListener);
        } catch( IOException e ) {
            throw new RuntimeException("Error creating server", e);
        }       
    }

    protected void joinGame() {
        log.info("joinGame()");
        getStateManager().attach(new ConnectionState(this, "127.0.0.1", port));
        setEnabled(false); // hide our window        
    }

    protected void stopHosting() {
        log.info("stopHosting()");        
        if( gameServer.getServer().isRunning() && !gameServer.getServer().getConnections().isEmpty() ) {
            String msg = "Really kick all " + gameServer.getServer().getConnections().size() + " connections?"; 
            getState(OptionPanelState.class).show("Disconnect", msg,
                                                  new CallMethodAction("Yes", this, "detach"),
                                                  new EmptyAction("No"),
                                                  new EmptyAction("Cancel"));            
        } else {
            // Just detach
            detach();
        } 
    }
    
    protected void detach() {
        getStateManager().detach(this);
    }

    @Override   
    protected void initialize( Application app ) {
        // We'll manage the server itself as part of the app state
        // lifecycle so that we can use enabled state for GUI elements
        // if we want.  Plus, the server will not be reusable once closed
        // and so neither will the state.  That fits better with the detach()
        // lifecycle than it does with enabled/disabled.
        gameServer.start();

        hostingState = getState(DebugHudState.class).createDebugValue("Hosting", Location.Right);
        hostingState.setObject("Online");
        
        connectionCount = getState(DebugHudState.class).createDebugValue("Connections", Location.Right);
        resetConnectionCount();
        
        hostWindow = new Container();
        
        // For now just something simple
        hostWindow.addChild(new Label("Hosting Control", new ElementId("title")));
        hostWindow.addChild(new ActionButton(new CallMethodAction("Join Game", this, "joinGame")));
        hostWindow.addChild(new ActionButton(new CallMethodAction("Stop Hosting", this, "stopHosting")));       
    }    

    @Override   
    protected void cleanup( Application app ) {
        gameServer.close("Shutting down.");
        hostingState.setObject("Offline");
        
        // And remove the debug messages anyway
        getState(DebugHudState.class).removeDebugValue("Hosting");
        getState(DebugHudState.class).removeDebugValue("Connections");
        
        // And re-enable the main menu
        getState(MainMenuState.class).setEnabled(true);
    }
    
    @Override   
    protected void onEnable() {
        Node gui = ((Main)getApplication()).getGuiNode();
 
        int height = getApplication().getCamera().getHeight();
        hostWindow.setLocalTranslation(10, height - 10, 0);       
        gui.attachChild(hostWindow);    
    }
    
    @Override   
    protected void onDisable() {
        hostWindow.removeFromParent();
    }
 
    protected void resetConnectionCount() {
        connectionCount.setObject(String.valueOf(gameServer.getServer().getConnections().size()));
    }
 
    private class ConnectionObserver implements ConnectionListener {
    
        public void connectionAdded( Server server, HostedConnection conn ) {
            resetConnectionCount();
        }
        
        public void connectionRemoved( Server server, HostedConnection conn ) {
            resetConnectionCount();
        }
    }
    
}
