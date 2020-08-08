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

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.simsilica.es.CreatedBy;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.Name;
import com.simsilica.es.base.DefaultEntityData;
import com.simsilica.es.common.Decay;
import com.simsilica.event.EventBus;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.SimTime;
import com.simsilica.sim.common.DecaySystem;
import com.simsilica.state.CompositeAppState;
import com.simsilica.state.GameSystemsState;

import sigem.GameConstants;
import sigem.GameSessionEvent;
import sigem.MainGameFunctions;
import sigem.es.Impulse;
import sigem.es.MassProperties;
import sigem.es.ObjectType;
import sigem.es.Position;
import sigem.es.ShipInput;
import sigem.es.SphereShape;
import sigem.sim.ArenaBoundary;
import sigem.sim.AsteroidHitListener;
import sigem.sim.CollisionSystem;
import sigem.sim.GameEntities;
import sigem.sim.MissileHitListener;
import sigem.sim.PlanetGravity;
import sigem.sim.PositionPublisher;
import sigem.sim.ShipInputSystem;
import sigem.sim.SimplePhysics;

/**
 *
 *
 *  @author    Paul Speed
 */
public class GameSessionState extends CompositeAppState {

    static Logger log = LoggerFactory.getLogger(GameSessionState.class);

    private GameSystemsState systems;
    private EntityData ed;
    private GameEntities gameEntities;

    public GameSessionState() {
        super(new GameSystemsState(),
              new CameraState(),
              new ModelViewState(),
              new PlayerInputState()
              );
            
        // Add states that need to support enable/disable independent of
        // the outer state using addChild().
        addChild(new InGameMenuState(false), true);
        
        // Setup the simulation... if we wait until initialize()
        // then the game system manager will have already been started
        // on the background thread.
        systems = getChild(GameSystemsState.class);
 
        // Setup example...

        // Setup the entity data
        this.ed = new DefaultEntityData();
        systems.register(EntityData.class, ed);
        
        // Add some systems
        systems.addSystem(new DecaySystem());
 
        systems.register(GameEntities.class, gameEntities = new GameEntities(ed));       
        systems.register(SimplePhysics.class, new SimplePhysics());
        systems.addSystem(new PlanetGravity());
        systems.register(CollisionSystem.class, new CollisionSystem());
        systems.register(AsteroidHitListener.class, new AsteroidHitListener());
        systems.register(MissileHitListener.class, new MissileHitListener());
        systems.addSystem(new PositionPublisher());
        systems.addSystem(new ShipInputSystem());
        systems.addSystem(new ArenaBoundary(GameConstants.ARENA_EXTENTS));   
        
        // Create some sample entities
        /*EntityId ship = ed.createEntity();
        ed.setComponents(ship,
            new Position(100, 0, 100),
            new MassProperties(1/50.0),
            ObjectType.create("ship", ed),
            new SphereShape(3, new Vec3d()),
            //new Impulse(new Vec3d(-5, 0, -5), new Vec3d(0, 1, 0)),
            //new ShipInput(new Vec3d(1, 0, 1))
            new ShipInput(new Vec3d())
            );*/
        //EntityId ship = createShip("Test", new Vec3d(100, 0, 100)); 
            
        EntityId planet = ed.createEntity();
        ed.setComponents(planet,
            new Position(0, 0, 0),
            new MassProperties(0),
            ObjectType.create("planet", ed),
            new SphereShape(8, new Vec3d())
            );
            
        Random rand = new Random(0);
        int asteroidCount = 20;
        Vec3d extents = GameConstants.ARENA_EXTENTS;
        for( int i = 0; i < asteroidCount; i++ ) {
            double x = rand.nextDouble() * extents.x * 2 - extents.x;     
            double z = rand.nextDouble() * extents.z * 2 - extents.z;
            
            double xImp = rand.nextDouble() * 8 - 4;     
            double zImp = rand.nextDouble() * 8 - 4;     
                        
            /*EntityId asteroid = ed.createEntity();
            ed.setComponents(asteroid,
                new Position(x, 0, z),
                new MassProperties(1/5.0),
                ObjectType.create("asteroid", ed),
                new SphereShape(3, new Vec3d()),
                new Impulse(new Vec3d(xImp, 0, zImp), new Vec3d(rand.nextDouble() + 1, rand.nextDouble(), 0))
                );*/
            int size = rand.nextInt(5) + 1;                
            createAsteroid(new Vec3d(x, 0, z), 
                           new Vec3d(xImp, 0, zImp),
                           new Vec3d(rand.nextDouble() + 1, rand.nextDouble(), 0),
                           size);                  
        }

            /*EntityId asteroid = ed.createEntity();
            ed.setComponents(asteroid,
                new Position(x, 0, z),
                new MassProperties(1/5.0),
                ObjectType.create("asteroid", ed),
                new SphereShape(3, new Vec3d()),
                new Impulse(new Vec3d(-4, 0, -4), new Vec3d(2, 0.5, 0))
                );*/
            
    }
 
    public EntityId createAsteroid( Vec3d location, Vec3d linVelocity, Vec3d angVelocity, double size ) {
 
        return gameEntities.createAsteroid(location, linVelocity, angVelocity, size);   
        /*double mass = size * size * size * 5;
    
        EntityId asteroid = ed.createEntity();
        ed.setComponents(asteroid,
            new Position(location),
            new MassProperties(1/mass),
            ObjectType.create("asteroid", ed),
            new SphereShape(size, new Vec3d()),
            new Impulse(linVelocity, angVelocity)
            );    
            
        return asteroid;*/
    }
 
    public EntityId createShip( String name, Vec3d location ) {
        EntityId ship = ed.createEntity();
        ed.setComponents(ship,
            new Position(location),
            new MassProperties(1/50.0),
            ObjectType.create("ship", ed),
            new SphereShape(2, new Vec3d()),
            new ShipInput(new Vec3d(0, 0, 0)),
            new Name(name)
            );
        return ship;
    }    
 
    public void shootMain( EntityId shooter ) {
        log.info("Bang");
        Position pos = ed.getComponent(shooter, Position.class);
        Vec3d dir = pos.getFacing().mult(Vec3d.UNIT_Z); 
        Vec3d loc = pos.getLocation().add(dir.mult(2));
        SimTime time = systems.getStepTime();
 
        double shotSpeed = 50;
        double shotTime = 2;
        
        EntityId missile = ed.createEntity();
        ed.setComponents(missile,
            new Position(loc),
            new MassProperties(1/5.0),
            ObjectType.create("missile", ed),
            new SphereShape(0, new Vec3d()),
            new CreatedBy(shooter),
            new Impulse(dir.mult(shotSpeed), new Vec3d()),
            new Decay(time.getTime(), time.getFutureTime(shotTime)) 
            );        
    }
 
    public void pause() {
        EventBus.publish(GameSessionEvent.sessionPaused, new GameSessionEvent());    
    }
    
    public void resume() {
        EventBus.publish(GameSessionEvent.sessionResumed, new GameSessionEvent());    
    }
    
    public void setInput( EntityId ship, ShipInput input ) {
        ed.setComponent(ship, input);
    }
    
    @Override
    protected void initialize( Application app ) {
    
        // Let other stuff know that the game session has started and
        // put the input mode into "IN GAME".        
        EventBus.publish(GameSessionEvent.sessionStarted, new GameSessionEvent());
                    
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup(MainGameFunctions.IN_GAME);
    }
    
    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup(MainGameFunctions.IN_GAME);
        
        EventBus.publish(GameSessionEvent.sessionEnded, new GameSessionEvent());
    }    
    
    @Override
    protected void onEnable() {
    }
    
    @Override
    protected void onDisable() {
        EventBus.publish(GameSessionEvent.sessionPaused, new GameSessionEvent());    
    }
}
