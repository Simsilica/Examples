/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.simsilica.es.EntityComponent;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Asser
 */
public class PhysicsVelocity implements EntityComponent{
    
    private Vector2 velocity;

    public PhysicsVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public Vector2 getVelocity() {
        return velocity;
    }
}
