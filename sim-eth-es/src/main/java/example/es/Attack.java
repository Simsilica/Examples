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
 * @author fahrenholza
 */
public class Attack implements EntityComponent {
    
    EntityId owner;

    public Attack(EntityId owner) {
        this.owner = owner;
    }

    public EntityId getOwner() {
        return owner;
    }
}
