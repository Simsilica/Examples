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

package example.net.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.network.HostedConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import com.simsilica.sim.GameLoop;
import com.simsilica.sim.GameSystemManager;

import example.GameConstants;

/**
 *  The main GameServer that manages the back end game services, hosts
 *  connections, etc..
 *
 *  @author    Paul Speed
 */
public class GameServer {

    static Logger log = LoggerFactory.getLogger(GameServer.class);
    
    private Server server;
    private GameSystemManager systems;
    private GameLoop loop;
    
    @SuppressWarnings("unused")
    private String description;
    
    public GameServer( int port, String description ) throws IOException {
        this.description = description;
        
        // Create the SpiderMonkey server and setup our standard
        // initial hosted services 
        this.server = Network.createServer(GameConstants.GAME_NAME, 
                                           GameConstants.PROTOCOL_VERSION,
                                           port, port);
        
        server.getServices().addServices(new RpcHostedService(),
                                         new RmiHostedService()
                                         //new AccountHostedService(),
                                         //new ArenaHost(),
                                         //new ChatHost((short)-2));
                                         );
        
        this.systems = new GameSystemManager();
        this.loop = new GameLoop(systems);
        
        // Add the various game services to the GameSystemManager
        
        // Add any hosted services that require those systems to already
        // exist
        
        log.info("Initializing game systems...");
        // Initialize the game system manager to prepare to start later
        systems.initialize();        
    }
    
    public Server getServer() {
        return server;
    }
    
    /**
     *  Starts the systems and begins accepting remote connections.
     */
    public void start() {
        log.info("Starting game server...");
        systems.start();
        server.start(); 
        loop.start();
        log.info("Game server started.");
    }
 
    /**
     *  Kicks all current connection, closes the network host, stops all systems, and 
     *  finally terminates them.  The GameServer is not restartable at this point.
     */   
    public void close( String kickMessage ) {
        log.info("Stopping game server..." + kickMessage);
        loop.stop();
        
        if( kickMessage != null ) {
            for( HostedConnection conn : server.getConnections() ) {
                conn.close(kickMessage);
            }
        }
        server.close();
        
        // The GameLoop dying should have already stopped the game systems
        if( systems.isInitialized() ) {
            systems.stop();
            systems.terminate();
        }
        log.info("Game server stopped.");
    }
    
    /**
     *  Closes the network host, stops all systems, and finally terminates
     *  them.  The GameServer is not restartable at this point.
     */   
    public void close() {
        close(null);
    }
    
    /**
     *  Allow running a basic dedicated server from the command line using
     *  the default port.  If we want something more advanced then we should
     *  break it into a separate class with a proper shell and so on.
     */
    public static void main( String... args ) throws Exception {
 
        GameServer gs = new GameServer(GameConstants.DEFAULT_PORT, "Dedicated Server");
        gs.start();                
                                                           
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while( (line = in.readLine()) != null ) {
            if( line.length() == 0 ) {
                continue;
            }
            if( "exit".equals(line) ) {
                break;
            } else {
                System.err.println("Unknown command:" + line);
            }
        }
        
        gs.close();
    }
}


