package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.mathd.Vec3d;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Asser
 */
public class WarpTouch implements EntityComponent {

    double targetAreaRadius; //The uncertainty of where you pop up
    Vec3d targetLocation; //The target area for warping to{

    public WarpTouch(double targetAreaRadius, Vec3d targetLocation) {
        this.targetAreaRadius = targetAreaRadius;
        this.targetLocation = targetLocation;
    }

    public WarpTouch(Vec3d targetLocation) {
        this.targetLocation = targetLocation;
        this.targetAreaRadius = 0.0d;
    }

    public double getTargetAreaRadius() {
        return targetAreaRadius;
    }

    public Vec3d getTargetLocation() {
        return targetLocation;
    }
}
