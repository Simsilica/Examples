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

package example.net.client;

import com.jme3.math.Quaternion;
import java.util.*;

import com.jme3.math.Vector3f;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;

import com.simsilica.es.*;
import com.simsilica.es.client.EntityDataClientService;

import com.simsilica.ethereal.EtherealClient;
import com.simsilica.ethereal.SharedObject;
import com.simsilica.ethereal.SharedObjectListener;

import org.slf4j.*;

import example.es.BodyPosition;


/**
 *  Updates the entities local position from network state.
 *  Requires that the entity have the BodyPosition component
 *  to accumulate state history for some backlog.  Updates to
 *  entities without a BodyPosition will be ignored... the alternative
 *  would be to cache a BodyPosition in advance until we finally
 *  see the entity.  This will be necessary if strict starting visibility
 *  is ever a requirement as the message that updates the entity's component
 *  may come some time after we've been recieving valid updates.  Enough that
 *  we'll be missing some history.  (For example, a missile might look like
 *  it starts a bit down its path.) 
 *
 *  @author    Paul Speed
 */
public class SharedObjectUpdater extends AbstractClientService
                                 implements SharedObjectListener {

    static Logger log = LoggerFactory.getLogger(SharedObjectUpdater.class);

    private EntityData ed;
    private EntitySet entities;
    private long frameTime;
    
    public SharedObjectUpdater() {
    }
    
    @Override
    protected void onInitialize(ClientServiceManager s) {
        log.info("onInitialize()");    
        this.ed = getService(EntityDataClientService.class).getEntityData();         
    }
    
    @Override
    public void start() {
        log.info("start()");    
        entities = ed.getEntities(BodyPosition.class);
        this.frameTime = -1;
        getService(EtherealClient.class).addObjectListener(this);
    }

    @Override
    public void stop() {
        log.info("stop()");    
        getService(EtherealClient.class).removeObjectListener(this);
        entities.release();
    }

    @Override
    public void beginFrame( long time ) {
        if( log.isTraceEnabled() ) {
            log.trace("** beginFrame(" + time + ")");
        }    
        this.frameTime = time;
        if( entities.applyChanges() ) {
            // Make sure the added/updated entities have been initialized
            initializeBodyPosition(entities.getAddedEntities());
            initializeBodyPosition(entities.getChangedEntities());
        }
    }
    
    protected void initializeBodyPosition( Set<Entity> set ) {
        for( Entity e : set ) {
            BodyPosition pos = e.get(BodyPosition.class);
            
            // BodyPosition requires special management to make
            // sure all instances of BodyPosition are sharing the same
            // thread-safe history buffer           
            pos.initialize(e.getId(), 12);
        }
    }

    @Override
    public void objectUpdated( SharedObject obj ) {
        if( log.isTraceEnabled() ) {
            log.trace("****** Object moved[t=" + frameTime + "]:" + obj.getEntityId() + "  pos:" + obj.getWorldPosition() + "  removed:" + obj.isMarkedRemoved());    
        }
        EntityId id = new EntityId(obj.getEntityId());
        Entity entity = entities.getEntity(id);
        if( entity == null ) {
            if( log.isDebugEnabled() ) {
                log.debug("No entity yet for:" + obj.getEntityId());
            }
            return;
        }        
        BodyPosition pos = entity.get(BodyPosition.class);        
        if( pos == null ) {
            // normal as it may take longer for that update to get here
            if( log.isDebugEnabled() ) {
                log.debug("Object doesn't have a BodyPosition yet for:" + obj.getEntityId());
            }
        } else {
            // Update our position buffer
            pos.addFrame(frameTime, 
                         obj.getWorldPosition().toVector3f(), 
                         obj.getWorldRotation().toQuaternion(),
                         true);
        }
    }

    @Override
    public void objectRemoved( SharedObject obj ) {
        if( log.isDebugEnabled() ) {
            log.debug("****** Object removed[t=" + frameTime + "]:" + obj.getEntityId());
        }
        EntityId id = new EntityId(obj.getEntityId());
        Entity entity = entities.getEntity(id);
        if( entity == null ) {
            if( log.isDebugEnabled() ) {
                log.debug("No entity for removed object for:" + obj.getEntityId());
            }
            return;
        }        
        BodyPosition pos = entity.get(BodyPosition.class);        
        if( pos == null ) {
            // normal as it may take longer for that update to get here
            if( log.isDebugEnabled() ) {
                log.debug("Removed object doesn't have a BodyPosition yet for:" + obj.getEntityId());
            }
        } else {
            if( log.isDebugEnabled() ) {            
                log.debug("Setting entity to invisible for:" + obj.getEntityId());
            }
            pos.addFrame(frameTime,  
                         obj.getWorldPosition().toVector3f(), 
                         obj.getWorldRotation().toQuaternion(),
                         false);
        }
    }

    @Override
    public void endFrame() {
        log.trace("** endFrame()");
        this.frameTime = -1;
    }

}


