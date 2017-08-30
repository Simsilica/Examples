/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

/**
 *
 * @author Asser
 */
public class SteeringSeek implements EntityComponent {
    
    EntityId target; 

    public EntityId getTarget() {
        return target;
    }

    public SteeringSeek(EntityId target) {
        this.target = target;
    }
}
