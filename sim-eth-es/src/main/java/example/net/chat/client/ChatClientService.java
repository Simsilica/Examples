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

package example.net.chat.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.*;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;

import example.net.chat.ChatSession;
import example.net.chat.ChatSessionListener;

/**
 *  Client-side service providing access to the chat server.
 *
 *  @author    Paul Speed
 */
public class ChatClientService extends AbstractClientService 
                                  implements ChatSession {

    static Logger log = LoggerFactory.getLogger(ChatClientService.class);
    
    private RmiClientService rmiService;
    private int channel;
    private ChatSession delegate;
    
    private String playerName;

    private ChatSessionCallback sessionCallback = new ChatSessionCallback();
    private List<ChatSessionListener> listeners = new CopyOnWriteArrayList<>();  
 
    /**
     *  Creates a new chat service that will use the default reliable
     *  channel for communication.
     */   
    public ChatClientService() {
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
 
    /**
     *  Creates a new chat service that will use the specified channel
     *  for any reliable communication.
     */   
    public ChatClientService( int channel ) {
        this.channel = channel;
    }

    @Override
    public void sendMessage( String message ) {
        //TODO: Test command line here (only test for '/')
        //TODO: ? for help related
        //TODO: * for account related
        getDelegate().sendMessage(message);
    }

    @Override
    public List<String> getPlayerNames() {
        return getDelegate().getPlayerNames();
    }        
        
    /**
     *  Adds a listener that will be notified about account-related events.
     *  Note that these listeners are called on the networking thread and
     *  as such are not suitable for modifying the visualization directly.
     */
    public void addChatSessionListener( ChatSessionListener l ) {
        listeners.add(l);
    }
    
    public void removeChatSessionListener( ChatSessionListener l ) {
        listeners.remove(l);
    }
 
    @Override
    protected void onInitialize( ClientServiceManager s ) {
        log.debug("onInitialize(" + s + ")");
        this.rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }
        log.debug("Sharing session callback.");  
        rmiService.share((byte)channel, sessionCallback, ChatSessionListener.class);
    }
 
    /**
     *  Called during connection setup once the server-side services have been initialized
     *  for this connection and any shared objects, etc. should be available.
     */   
    @Override
    public void start() {
        log.debug("start()");
        super.start();
    }
    
    private ChatSession getDelegate() {
        // We look up the delegate lazily to make the service more
        // flexible.  This way we don't have to know anything about the
        // connection lifecycle and can simply report an error if the
        // game is doing something screwy.
        if( delegate == null ) {
            // Look it up
            this.delegate = rmiService.getRemoteObject(ChatSession.class);
            log.debug("delegate:" + delegate);       
            if( delegate == null ) {
                throw new RuntimeException("No chat session found");
            }            
        }
        return delegate;
    }
    
    /**
     *  Shared with the server over RMI so that it can notify us about account
     *  related stuff.
     */
    private class ChatSessionCallback implements ChatSessionListener {
 
        @Override   
        public void playerJoined( int clientId, String playerName ) {
            if( log.isTraceEnabled() ) {            
                log.trace("playerJoined(" + clientId + ", " + playerName + ")");
            }
            for( ChatSessionListener l : listeners ) {
                l.playerJoined(clientId, playerName);
            }
        }
 
        @Override   
        public void newMessage( int clientId, String playerName, String message ) {
            if( log.isTraceEnabled() ) {            
                log.trace("newMessage(" + clientId + ", " + playerName + ", " + message + ")");
            }
            for( ChatSessionListener l : listeners ) {
                l.newMessage(clientId, playerName, message);
            }
        }
    
        @Override   
        public void playerLeft( int clientId, String playerName ) {
            if( log.isTraceEnabled() ) {            
                log.trace("playerLeft(" + clientId + ", " + playerName + ")");
            }
            for( ChatSessionListener l : listeners ) {
                l.playerLeft(clientId, playerName);
            }
        }
    }
}


