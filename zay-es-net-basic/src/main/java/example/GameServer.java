/*
 * $Id$
 * 
 * Copyright (c) 2015, Simsilica, LLC
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

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.serializers.FieldSerializer;
import com.simsilica.es.Name;
import com.simsilica.es.base.DefaultEntityData;
import com.simsilica.es.server.EntityDataHostedService;


/**
 *
 *
 *  @author    Paul Speed
 */
public class GameServer {
 
    private static final String[] SPLASH = {   
        "  _____                 _____ ____    ____",                           
        " |__  /__ _ _   _      | ____/ ___|  / ___|  ___ _ ____   _____ _ __", 
        "   / // _` | | | |_____|  _| \\___ \\  \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|",
        "  / /| (_| | |_| |_____| |___ ___) |  ___) |  __/ |   \\ V /  __/ |",   
        " /____\\__,_|\\__, |     |_____|____/  |____/ \\___|_|    \\_/ \\___|_|",          
        "            |___/",
        "    Zay-ES-Net Server Example"
    };
 
    public static void printSplash() {
        for( String s : SPLASH ) {
            System.out.println(s);
        }
    }

    public static void main( String... args ) throws Exception {
    
        // Initialize JUL -> sl4j logging
        LogUtil.initialize();
    
        printSplash();
 
        // Create the basic SpiderMonkey server
        Server server = Network.createServer(GameConstants.NAME, 
                                             GameConstants.PROTOCOL_VERSION,
                                             GameConstants.PORT, GameConstants.PORT);
        
        // Setup our entity data and the hosting service
        DefaultEntityData ed = new DefaultEntityData();
        server.getServices().addService(new EntityDataHostedService(MessageConnection.CHANNEL_DEFAULT_RELIABLE, ed));
        
        // Register some components with the serializer
        // Normally it's best to have static methods to do this
        Serializer.registerClass(Position.class, new FieldSerializer());
        Serializer.registerClass(Name.class, new FieldSerializer());
 
        // Something simple to create some pieces and move them around
        SimpleGameLogic gameLogic = new SimpleGameLogic(ed);
 
        // Start the server
        server.start();
 
        System.out.println("Server started.  Press Ctrl-C to stop.");
 
        try {        
            // Wait indefinitely, updating the game loop and entities periodically
            while( true ) {
 
                gameLogic.update();
                          
                server.getServices().getService(EntityDataHostedService.class).sendUpdates();
                               
                Thread.sleep(100); // 10 times a second
                
                // Note: normally the game logic would get updated more often than
                //       the entity data send updates.  For example, game logic updates
                //       at 60 FPS while updates are sent only 20 FPS or less depending
                //       on the type of game.
            }
        } finally {
            // We don't have a way to shutdown gracefully from our game loop but if
            // we did, this is a good way to shut everything down.         
            System.out.println("Closing connections...");
            // Kick the connections
            for( HostedConnection conn : server.getConnections() ) {
                conn.close("Shutting down.");
            }
            System.out.println("Shutting down server...");
            server.close();
        }       
    }
}
