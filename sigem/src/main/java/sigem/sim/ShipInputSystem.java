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

package sigem.sim;

import java.util.*;

import org.slf4j.*;

import com.simsilica.es.*;
import com.simsilica.es.common.Decay;
import com.simsilica.mathd.*;
import com.simsilica.sim.*;

import sigem.es.*;

/**
 *  Manages the various ShipInput entities and makes sure their
 *  associated Body's ShipDrivers stay updated.
 *
 *  @author    Paul Speed
 */
public class ShipInputSystem extends AbstractGameSystem {
 
    static Logger log = LoggerFactory.getLogger(ShipInputSystem.class);
    
    private EntityData ed;
    private SimplePhysics phys;
    
    private ShipContainer ships;
    
    private double nextPuff = 0;
    //private double puffInterval = 0.05; //0.2; 
    private double puffInterval = 0.0125; //0.2; 
        
    public ShipInputSystem() {
    }

    @Override    
    protected void initialize() {
        this.ed = getSystem(EntityData.class);
        if( ed == null ) {
            throw new RuntimeException("ShipInputSystem requires an EntityData object.");
        }
        this.phys = getSystem(SimplePhysics.class);
        if( ed == null ) {
            throw new RuntimeException("ShipInputSystem system requires a SimplePhysics object.");
        }
    }
    
    @Override    
    protected void terminate() {
    }
 
    @Override
    public void start() {
        ships = new ShipContainer(ed);
        ships.start();
    }

    @Override
    public void stop() {
        ships.stop();
        ships = null;
    }

    @Override
    public void update( SimTime time ) {
        ships.update();
        
        // Could have done this with a separate system but
        // it was convenient to do it here.
        double t = time.getTimeInSeconds(); 
        if( t > nextPuff ) {
            nextPuff = t + puffInterval;
            
            for( ShipDriver ship : ships.getArray() ) {
                if( ship.getThrust().z <= 0.25 ) {
                    continue;
                }
                // Else the thrusters are on and we can puff
                Body body = ship.getBody();
                // Make it appear 2 units behind the ship
                Vec3d loc = body.pos.subtract(body.orientation.mult(Vec3d.UNIT_Z.mult(2)));
                EntityId puff = ed.createEntity();
                ed.setComponents(puff,
                    new Position(loc),
                    ObjectType.create("thrust", ed),
                    new Decay(time.getTime(), time.getFutureTime(2.5))
                    );
            } 
        } 
    }
    
    private class ShipContainer extends EntityContainer<ShipDriver> {
        public ShipContainer( EntityData ed ) {
            super(ed, ShipInput.class);
        }
        
        public ShipDriver[] getArray() {
            return super.getArray();
        }
 
        @Override       
        protected ShipDriver addObject( Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("addObject(" + e + ")");
            }        
            ShipDriver driver = new ShipDriver();
            updateObject(driver, e);
            phys.setControlDriver(e.getId(), driver);            
            return driver;
        }
        
        @Override       
        protected void updateObject( ShipDriver object, Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("updateObject(" + e + ")");
            }
            object.applyControlInput(e.get(ShipInput.class));
        }
        
        @Override       
        protected void removeObject( ShipDriver object, Entity e ) {
            if( log.isTraceEnabled() ) {
                log.trace("removeObject(" + e + ")");
            }        
        }
    }
}
