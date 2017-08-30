/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.jme3.math.FastMath;
import com.simsilica.es.EntityComponent;
import com.simsilica.mathd.Vec3d;

/**
 *
 * @author Asser
 */
public class GravityWell implements EntityComponent {

    public final static String PULL = "pull";
    public final static String PUSH = "push";
    
    private double distance;
    private double force;
    private String gravityType;
    

    public GravityWell(double distance, double force, String gravityType) {
        this.distance = distance;
        this.force = Math.abs(force);
        this.gravityType = gravityType;
    }

    public double getDistance() {
        return distance;
    }

    public double getForce() {
        return force;
    }

    public String getGravityType() {
        return gravityType;
    }

}
