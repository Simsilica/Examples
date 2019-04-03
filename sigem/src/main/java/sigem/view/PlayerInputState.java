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

package sigem.view;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

import com.simsilica.es.*;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.*;
import com.simsilica.mathd.*;

import sigem.es.*;

/**
 *  Maps player inputs to ships.
 *
 *  @author    Paul Speed
 */
public class PlayerInputState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(PlayerInputState.class);

    private GameSessionState gameSession;
    
    private EntityId ship1;
    private ShipHandler handler1;

    private EntityId ship2;
    private ShipHandler handler2;

    public PlayerInputState() {        
    }
    
    @Override
    protected void initialize( Application app ) {
        this.gameSession = getState(GameSessionState.class);     
        //this.ship1 = gameSession.createShip("Player 1", new Vec3d(50, 0, 50));        
        //this.ship1 = gameSession.createShip("Player 1", new Vec3d(150, 0, 85));        
        this.ship1 = gameSession.createShip("Player 1", new Vec3d(-195, 0, 100));        
        this.handler1 = new ShipHandler(ship1, 
                                        PlayerMovementFunctions.F_P1_THRUST,
                                        PlayerMovementFunctions.F_P1_TURN,
                                        PlayerMovementFunctions.F_P1_SHOOT_MAIN);                    
                                        
        this.ship2 = gameSession.createShip("Player 2", new Vec3d(100, 0, 100));        
        this.handler2 = new ShipHandler(ship2, 
                                        PlayerMovementFunctions.F_P2_THRUST,
                                        PlayerMovementFunctions.F_P2_TURN,
                                        PlayerMovementFunctions.F_P2_SHOOT_MAIN);                    
    }
    
    @Override
    protected void cleanup( Application app ) {
    }
    
    @Override
    protected void onEnable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
 
        handler1.addListeners(inputMapper);       
        handler2.addListeners(inputMapper);       
    }
    
    @Override
    protected void onDisable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        handler1.removeListeners(inputMapper);       
        handler2.removeListeners(inputMapper);       
    }
    
    @Override
    public void update( float tpf ) {
        handler1.update();
        handler2.update();
    }
    
    private class ShipHandler implements AnalogFunctionListener, StateFunctionListener {
    
        private EntityId ship;
        private Vec3d thrust = new Vec3d();
        private FunctionId fThrust;
        private FunctionId fTurn;
        private FunctionId fMain;
        
        private double shotInterval = 0.5;
        private double shotTimer;
        
        public ShipHandler( EntityId ship, FunctionId fThrust, FunctionId fTurn, FunctionId fMain ) {        
            this.ship = ship;
            this.fThrust = fThrust;
            this.fTurn = fTurn;
            this.fMain = fMain;
        }

        public void update() {
            gameSession.setInput(ship, new ShipInput(thrust));
        }
        
        public void addListeners( InputMapper inputMapper ) {
            inputMapper.addAnalogListener(this, fThrust, fTurn, fMain);
            inputMapper.addStateListener(this, fMain);           
        }

        public void removeListeners( InputMapper inputMapper ) {
            inputMapper.removeAnalogListener(this, fThrust, fTurn, fMain);           
            inputMapper.removeStateListener(this, fMain);
        }
    
        @Override
        public void valueChanged( FunctionId func, InputState value, double tpf ) {
            if( log.isTraceEnabled() ) {
                log.trace("valueChanged:" + func + "  value:" + value);
            }
            //log.info("value changed:" + func + "  value:" + value);
            if( func == fMain ) {
                // Shoot immediately when the button is pressed
                shotTimer = shotInterval;
            }  
        }

        @Override
        public void valueActive( FunctionId func, double value, double tpf ) {
            if( log.isTraceEnabled() ) {
                log.trace("valueActive:" + func + "  value:" + value);
            } 
            if( func == fThrust ) {
                thrust.z = value;
            } else if( func == fTurn ) {
                thrust.x = value;
            } else if( func == fMain ) {
                shotTimer += tpf;
                if( shotTimer > shotInterval ) {
                    shotTimer = 0;
                    gameSession.shootMain(ship);
                }
            }
        }
    }

}
