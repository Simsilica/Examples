package example.sim;

import com.simsilica.es.EntityId;

/**
 *
 * @author Asser
 */
public interface EntityCollisionListener {

    /**
     * Method for listening for physical collision between two entities
     * @param eId1 First entity to collide
     * @param eId2 Second entity to collide
     */
    public abstract void collision(EntityId eId1, EntityId eId2);

}
