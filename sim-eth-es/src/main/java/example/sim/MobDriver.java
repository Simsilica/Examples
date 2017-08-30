package example.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.dyn4j.geometry.Vector2;

/**
 * Uses rotation and a 3-axis thrust vector to supply specific velocity to a
 * body. We ignore the normal physics acceleration for now and just set the
 * velocity directly based on our accelerated thrust values.
 *
 * @author Paul Speed
 */
public class MobDriver implements ControlDriver {

    // Keep track of what the player has provided.
    private volatile Quaternion orientation = new Quaternion();
    private volatile Vector3f thrust = new Vector3f();

    private double pickup = 3;

    public void applyMovementState(Vector3f thrust) {
        this.thrust = thrust;
    }

    @Override
    public void update(double stepTime, SimpleBody body) {
        
        // add force
        if (thrust.x != 0 || thrust.y != 0) {
            Vector2 linVel = new Vector2(thrust.x, thrust.y);
            body.applyForce(linVel);

            // add rotation
            double desired_dir = rad_between_vec(1, 0, thrust.x, thrust.y); // [rad]
            double orientaion = Math.atan2(body.orientation.toRotationMatrix().m00, body.orientation.toRotationMatrix().m01); // [rad]

            double diff = desired_dir - orientaion; // [rad]

            if (diff < Math.PI) {
                body.setAngularVelocity(diff * 2);
            } else {
                body.setAngularVelocity(-diff * 2);
            }
        }
    }

    private double rad_between_vec(double x1, double y1, double x2, double y2) {
        return Math.signum(y2) * Math.acos((x1 * x2 + y1 * y2) / (Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2)));
    }
}
