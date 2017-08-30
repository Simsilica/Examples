package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
import com.simsilica.mathd.Vec3d;
import org.dyn4j.dynamics.Force;
import org.dyn4j.dynamics.Torque;
import org.dyn4j.geometry.Vector2;

/**
 * Represents the linear and angular velocity for an entity.
 *
 * @author Asser Fahrenholz
 */
public class PhysicsForce implements EntityComponent {

    private Force force;
    private EntityId target;
    private Vector2 forceWorldCoords;

    public PhysicsForce() {
    }

    public PhysicsForce(EntityId target, Force force, Vector2 forceWorldCoords) {
        this.force = force;
        this.target = target;
        this.forceWorldCoords = forceWorldCoords;
    }

    public Vector2 getForceWorldCoords() {
        return forceWorldCoords;
    }

    public EntityId getTarget() {
        return target;
    }

    public Force getForce() {
        return force;
    }

    @Override
    public String toString() {
        return "PhysicsForce{" + "force=" + force + ", target=" + target + ", forceWorldCoords=" + forceWorldCoords + '}';
    }
}
