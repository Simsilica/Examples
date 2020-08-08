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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.network.HostedConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.simsilica.event.EventBus;

import example.net.AccountSession;
import example.net.AccountSessionListener;

/**
 *  Provides super-basic account services like logging in.  This could
 *  be expanded to be more complicated based on a real game's needs.
 *  The basics have been included here as a minimal example that includes
 *  the basic types of communication necessary.
 *
 *  @author    Paul Speed
 */
public class AccountHostedService extends AbstractHostedConnectionService {

    static Logger log = LoggerFactory.getLogger(AccountHostedService.class);

    private static final String ATTRIBUTE_SESSION = "account.session";
    private static final String ATTRIBUTE_PLAYER_NAME = "account.playerName";

    private RmiHostedService rmiService;
 
    private String serverInfo;
    
    public AccountHostedService( String serverInfo ) {
        this.serverInfo = serverInfo;
    }
    
    public static String getPlayerName( HostedConnection conn ) {
        return conn.getAttribute(ATTRIBUTE_PLAYER_NAME);   
    }
 
    @Override
    protected void onInitialize( HostedServiceManager s ) {
        
        // Grab the RMI service so we can easily use it later        
        this.rmiService = getService(RmiHostedService.class);
        if( rmiService == null ) {
            throw new RuntimeException("AccountHostedService requires an RMI service.");
        }
    }
    
    @Override
    public void startHostingOnConnection( HostedConnection conn ) {
        
        log.debug("startHostingOnConnection(" + conn + ")");
    
        AccountSessionImpl session = new AccountSessionImpl(conn);
        conn.setAttribute(ATTRIBUTE_SESSION, session);
        
        // Expose the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(conn);
        rmi.share(session, AccountSession.class);
    }
 
    @Override   
    public void stopHostingOnConnection( HostedConnection conn ) {
        log.debug("stopHostingOnConnection(" + conn + ")");
        String playerName = getPlayerName(conn);
        if( playerName != null ) {
            log.debug("publishing playerLoggedOff event for:" + conn);
            // Was really logged on before
            EventBus.publish(AccountEvent.playerLoggedOff, new AccountEvent(conn, playerName));            
        }
    }
 
    /**
     *  The connection-specific 'host' for the AccountSession.
     */ 
    private class AccountSessionImpl implements AccountSession {
 
        private HostedConnection conn;
        private AccountSessionListener callback;
        
        public AccountSessionImpl( HostedConnection conn ) {
            this.conn = conn;
            
            // Note: at this point we won't be able to look up the callback
            // because we haven't received the client's RMI shared objects yet.
        }
 
        protected AccountSessionListener getCallback() {
            if( callback == null ) {
                RmiRegistry rmi = rmiService.getRmiRegistry(conn);
                callback = rmi.getRemoteObject(AccountSessionListener.class);
                if( callback == null ) {
                    throw new RuntimeException("Unable to locate client callback for AccountSessionListener");
                }
            }
            return callback;
        } 
 
        @Override   
        public String getServerInfo() {
            return serverInfo;
        } 
 
        @Override   
        public void login( String playerName ) {
            log.info("login(" + playerName + ")");
            conn.setAttribute(ATTRIBUTE_PLAYER_NAME, playerName);
            
            // And let them know they were successful
            getCallback().notifyLoginStatus(true);
            
            log.debug("publishing playerLoggedOn event for:" + conn);
            // Notify 'logged in' only after we've told the player themselves            
            EventBus.publish(AccountEvent.playerLoggedOn, new AccountEvent(conn, playerName));            
        }
    }    
}


