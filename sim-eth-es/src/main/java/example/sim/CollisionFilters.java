/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.sim;

import org.dyn4j.collision.CategoryFilter;

/**
 *
 * @author Asser
 */
public class CollisionFilters {

    /**
     * Static bodies collides with all bodies except other static bodies.
     */
    private static final long COLLISION_CATEGORY_STATIC_BODIES = 1;
    private static final long COLLISION_CATEGORY_STATIC_GRAVITY = 2;
    private static final long COLLISION_CATEGORY_DYNAMICS_PLAYERS = 4;
    private static final long COLLISION_CATEGORY_DYNAMICS_PROJECTILES = 8;
    private static final long COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS = 16;
    private static final long COLLISION_CATEGORY_SENSOR_FLAGS = 32;
    private static final long COLLISION_CATEGORY_STATIC_TOWERS = 64;
    private static final long COLLISION_CATEGORY_DYNAMICS_MOBS = 128;
    private static final long COLLISION_CATEGORY_STATIC_BASE = 256;
    private static final long COLLISION_CATEGORY_ALL = Long.MAX_VALUE;

    /**
     * Static bodies collides with all dynamics
     */
    public static final CategoryFilter FILTER_CATEGORY_STATIC_BODIES = new CategoryFilter(COLLISION_CATEGORY_STATIC_BODIES,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES
            | COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS
            | COLLISION_CATEGORY_DYNAMICS_MOBS);

    /**
     * Gravities collides with all dynamics
     */
    public static final CategoryFilter FILTER_CATEGORY_STATIC_GRAVITY = new CategoryFilter(COLLISION_CATEGORY_STATIC_GRAVITY,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES
            | COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS
            | COLLISION_CATEGORY_DYNAMICS_MOBS);

    /**
     * Dynamic players collide with everything except other players
     */
    public static final CategoryFilter FILTER_CATEGORY_DYNAMIC_PLAYERS = new CategoryFilter(COLLISION_CATEGORY_DYNAMICS_PLAYERS,
            COLLISION_CATEGORY_STATIC_GRAVITY
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES
            | COLLISION_CATEGORY_STATIC_BODIES
            | COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS
            | COLLISION_CATEGORY_SENSOR_FLAGS
            | COLLISION_CATEGORY_DYNAMICS_MOBS);

    /**
     * Dynamic projectiles collide with everything except other projectiles
     */
    public static final CategoryFilter FILTER_CATEGORY_DYNAMIC_PROJECTILES = new CategoryFilter(COLLISION_CATEGORY_DYNAMICS_PROJECTILES,
            COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS
            | COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_STATIC_GRAVITY
            | COLLISION_CATEGORY_STATIC_BODIES
            | COLLISION_CATEGORY_DYNAMICS_MOBS
            | COLLISION_CATEGORY_STATIC_TOWERS
            | COLLISION_CATEGORY_STATIC_BASE);

    /**
     * Dynamic map objects collide with everything
     */
    public static final CategoryFilter FILTER_CATEGORY_DYNAMIC_MAPOBJECTS = new CategoryFilter(COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS,
            COLLISION_CATEGORY_ALL);

    /**
     * Flags collide only with players
     */
    public static final CategoryFilter FILTER_CATEGORY_SENSOR_FLAGS = new CategoryFilter(COLLISION_CATEGORY_SENSOR_FLAGS,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS);

    /**
     * Towers collides players, mobs and projectiles
     */
    public static final CategoryFilter FILTER_CATEGORY_STATIC_TOWERS = new CategoryFilter(COLLISION_CATEGORY_STATIC_TOWERS,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_DYNAMICS_MOBS
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES
            | COLLISION_CATEGORY_STATIC_TOWERS);

    /**
     * Base senses players and mobs
     */
    public static final CategoryFilter FILTER_CATEGORY_STATIC_BASE = new CategoryFilter(COLLISION_CATEGORY_STATIC_BASE,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_DYNAMICS_MOBS
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES);

    /**
     * Mobs collides with players, projectiles, map objects, but not themselves
     * (should be able to stack on top of each other)
     */
    public static final CategoryFilter FILTER_CATEGORY_DYNAMIC_MOBS = new CategoryFilter(COLLISION_CATEGORY_DYNAMICS_MOBS,
            COLLISION_CATEGORY_DYNAMICS_PLAYERS
            | COLLISION_CATEGORY_DYNAMICS_PROJECTILES
            | COLLISION_CATEGORY_DYNAMICS_MAPOBJECTS
            | COLLISION_CATEGORY_STATIC_TOWERS
            | COLLISION_CATEGORY_STATIC_BASE);
}
