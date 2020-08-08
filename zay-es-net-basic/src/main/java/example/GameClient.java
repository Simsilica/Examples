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

import java.util.concurrent.CountDownLatch;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.MessageConnection;
import com.jme3.network.Network;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;
import com.simsilica.es.client.EntityDataClientService;

/**
 *
 *
 *  @author    Paul Speed
 */
public class GameClient {

    private static final String[] SPLASH = {   
        "  _____                 _____ ____     ____ _ _            _",   
        " |__  /__ _ _   _      | ____/ ___|   / ___| (_) ___ _ __ | |_", 
        "   / // _` | | | |_____|  _| \\___ \\  | |   | | |/ _ \\ '_ \\| __|",
        "  / /| (_| | |_| |_____| |___ ___) | | |___| | |  __/ | | | |_", 
        " /____\\__,_|\\__, |     |_____|____/   \\____|_|_|\\___|_| |_|\\__|",
        "            |___/",
        "    Zay-ES-Net Client Example"                                              
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
        
        Client client = Network.connectToServer(GameConstants.NAME, 
                                                GameConstants.PROTOCOL_VERSION,
                                                "localhost",
                                                GameConstants.PORT, GameConstants.PORT);
        client.getServices().addService(new EntityDataClientService(MessageConnection.CHANNEL_DEFAULT_RELIABLE));

        // Can grab this even before started but you won't be able to retrieve
        // entities until the connection has been fully setup.
        EntityData ed = client.getServices().getService(EntityDataClientService.class).getEntityData();
         
        // Normally you wouldn't even kick off the networking stuff except
        // as a response to the ClientStateListener but in this example we'll
        // simplify things.  This code will block execution of this thread and
        // that's normally bad in a real application. 
        final CountDownLatch startedSignal = new CountDownLatch(1);
        client.addClientStateListener(new ClientStateListener() {
                @Override
                public void clientConnected( Client c) {
                    startedSignal.countDown();
                }
                
                @Override
                public void clientDisconnected( Client c, ClientStateListener.DisconnectInfo info ) {
                    System.out.println("Client disconnected.");
                }
            });
 
        client.start();
 
 
        // Wait for the client to start
        System.out.println("Waiting for connection setup.");
        startedSignal.await();
        System.out.println("Connected.");
        System.out.println("Press Ctrl-C to stop or wait 60 seconds.");
        
        // Let's get some entities
        EntitySet entities = ed.getEntities(Name.class, Position.class);

        if( !entities.isEmpty() ) {
            // Should always be empty at this point because filling out the
            // set is lazy and we've got nothing possibly cached yet.
            System.out.println("Initial entities:");
            for( Entity e : entities.getAddedEntities() ) {
                System.out.println("    " + e);
            }
        } 
        
        // We will watch for 60 seconds of 60 frames each and then disconnect
        for( int i = 0; i < 60 * 60; i++ ) {
            
            if( entities.applyChanges() ) {
                if( !entities.getAddedEntities().isEmpty() ) {
                    System.out.println("Added:");
                    for( Entity e : entities.getAddedEntities() ) {
                        System.out.println("    " + e);
                    } 
                }
                if( !entities.getChangedEntities().isEmpty() ) { 
                    System.out.println("Changed:");
                    for( Entity e : entities.getChangedEntities() ) {
                        System.out.println("    " + e);
                    } 
                }
                if( !entities.getRemovedEntities().isEmpty() ) { 
                    System.out.println("Removed:");
                    for( Entity e : entities.getRemovedEntities() ) {
                        System.out.println("    " + e.getId());
                    } 
                }
            }
            
            // Pause for 1/60th of a second
            Thread.sleep(1000/60);
        }
 
        System.out.println("Closing client.");                                               
        client.close();                                                       
    }
}
