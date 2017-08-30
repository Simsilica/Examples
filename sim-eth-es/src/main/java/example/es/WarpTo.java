package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.mathd.Vec3d;

/**
 *
 * @author Asser
 */
public class WarpTo implements EntityComponent {

    Vec3d targetLocation;

    public WarpTo(Vec3d targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Vec3d getTargetLocation() {
        return targetLocation;
    }
}
