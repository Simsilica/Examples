/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.sim;

import com.simsilica.es.EntityComponent;
import example.PhysicsConstants;
import example.es.PhysicsShape;
import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;

/**
 *
 * @author Asser
 */
public class PhysicsShapes {

    public static PhysicsShape tower() {

        Filter filter = CollisionFilters.FILTER_CATEGORY_STATIC_TOWERS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.TOWERSIZERADIUS));
        fixture.setFilter(filter);
        //fixture.setSensor(true);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape ship() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_PLAYERS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.SHIPSIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape bounty() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_MAPOBJECTS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.BOUNTYSIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape bomb() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_PROJECTILES;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.BOMBSIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape bullet() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_PROJECTILES;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.BULLETSIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape mapTile(Convex c) {

        Filter filter = CollisionFilters.FILTER_CATEGORY_STATIC_BODIES;
        BodyFixture fixture = new BodyFixture(c);
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape wormhole() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_STATIC_GRAVITY;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.WORMHOLESIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape over5() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_STATIC_BODIES;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.OVER5SIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape over1() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_MAPOBJECTS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.OVER1SIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }

    public static PhysicsShape over2() {
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_MAPOBJECTS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.OVER2SIZERADIUS));
        fixture.setFilter(filter);

        return new PhysicsShape(fixture);
    }
    
    public static PhysicsShape flag(){
        Filter filter = CollisionFilters.FILTER_CATEGORY_SENSOR_FLAGS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.FLAGSIZERADIUS));
        fixture.setFilter(filter);
        fixture.setSensor(true);

        return new PhysicsShape(fixture);
    }
    
    public static PhysicsShape mob(){
        Filter filter = CollisionFilters.FILTER_CATEGORY_DYNAMIC_MOBS;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.MOBSIZERADIUS));        
        fixture.setFilter(filter);
        //fixture.setSensor(true);

        return new PhysicsShape(fixture);
    }
    
    public static PhysicsShape base(){
        Filter filter = CollisionFilters.FILTER_CATEGORY_STATIC_BASE;
        BodyFixture fixture = new BodyFixture(Geometry.createPolygonalCircle(PhysicsConstants.VERTEXCOUNTCIRCLE, PhysicsConstants.BASESIZERADIUS));
        fixture.setFilter(filter);
        //fixture.setSensor(true);

        return new PhysicsShape(fixture);
    }
}
