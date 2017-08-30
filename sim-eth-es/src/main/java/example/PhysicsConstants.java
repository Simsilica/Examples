/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

/**
 *
 * @author Asser
 */
public class PhysicsConstants {
    
    
    public static double PHYSICS_SCALE = 1;
    
    //Radius
    public final static double BULLETSIZERADIUS = 0.125f;
    public final static double BOMBSIZERADIUS = 0.5f;
    public final static double BOUNTYSIZERADIUS = 0.5f;
    public final static double SHIPSIZERADIUS = 1f;
    public final static double MOBSIZERADIUS = 1f;
    public final static double TOWERSIZERADIUS = 1f;
    public final static double BASESIZERADIUS = 2.5f;
    public final static double WORMHOLESIZERADIUS = 0.1;
    public final static double OVER5SIZERADIUS = 0.1;
    public final static double OVER1SIZERADIUS = 0.5;
    public final static double OVER2SIZERADIUS = 1;
    public final static double FLAGSIZERADIUS = 0.5;
    
    
    //Weights
    public final static double SHIPMASS = 50;
    public final static double BOMBMASS = 25;
    public final static double BULLETMASS = 5;
    public final static double MAPTILEMASS = 0; //Infinite mass
    public final static double WORMHOLEMASS = 0;
    public final static double OVER1MASS = 10;
    public final static double OVER2MASS = 40;
    public final static double OVER5MASS = 0;
    
    //View
    public final static double PROJECTILEOFFSET = 3;
    
    //Forces
    public final static float SHIPTHRUST = 10;
    
    //Pathfinding and polygons
    public final static int VERTEXCOUNTCIRCLE = 10;
}
