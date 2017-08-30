package example.es;

import com.simsilica.es.EntityComponent;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.BodyFixture;

/**
 *
 * @author Asser
 */
public class PhysicsShape implements EntityComponent {

    BodyFixture fixture;

    //Doesn't need empty serialization constructor because it will not be sent to client
    public PhysicsShape(BodyFixture fixture) {
        this.fixture = fixture;
    }

    public BodyFixture getFixture() {
        return fixture;
    }

}
