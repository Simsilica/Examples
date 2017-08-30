package example.sim;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
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
public class GDXAIDriver implements ControlDriver, Steerable<com.badlogic.gdx.math.Vector2> {

    // Keep track of what the player has provided.
    private volatile Quaternion orientation = new Quaternion();
    private volatile Vector3f thrust = new Vector3f();

    //Variables for the steering behaviours-->
    private SimpleBody steerableBody;
    private boolean tagged;
    private float zeroLinearSpeedThreshold;
    private float maxLinearSpeed = 1;
    private float maxLinearAcceleration = 1;
    private float maxAngularSpeed = 1;
    private float maxAngularAcceleration = 1;
    //<<--

    private double pickup = 3;
    private Vector3f angular;

    public GDXAIDriver(SimpleBody steerableBody) {
        this.steerableBody = steerableBody;
    }

    public void applyMovementState(Vector3f thrust, Vector3f angular) {
        this.thrust = thrust;
        this.angular = angular;
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

    private com.badlogic.gdx.math.Vector2 convert(Vector2 vec2) {
        return new com.badlogic.gdx.math.Vector2((float) vec2.x, (float) vec2.y);
    }

    private float convert(double d) {
        return (float) d;
    }

    @Override
    public com.badlogic.gdx.math.Vector2 getLinearVelocity() {
        return convert(steerableBody.getLinearVelocity());
    }

    @Override
    public float getAngularVelocity() {
        return convert(steerableBody.getAngularVelocity());
    }

    @Override
    public float getBoundingRadius() {
        return convert(steerableBody.getRotationDiscRadius());
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean bln) {
        tagged = bln;
    }

    @Override
    public com.badlogic.gdx.math.Vector2 getPosition() {
        return convert(steerableBody.getTransform().getTranslation());
    }

    @Override
    public float getOrientation() {
        return convert(steerableBody.getTransform().getRotation());
    }

    @Override
    public void setOrientation(float f) {
        steerableBody.getTransform().setRotation(convert(f));
    }

    // Here we assume the y-axis is pointing upwards.
    @Override
    public float vectorToAngle(com.badlogic.gdx.math.Vector2 t) {
        return (float) Math.atan2(-t.x, t.y);
    }

    @Override
    public com.badlogic.gdx.math.Vector2 angleToVector(com.badlogic.gdx.math.Vector2 t, float f) {
        t.x = -(float) Math.sin(f);
        t.y = (float) Math.cos(f);
        return t;
    }

    @Override
    public Location<com.badlogic.gdx.math.Vector2> newLocation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float f) {
        zeroLinearSpeedThreshold = f;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float f) {
        maxLinearSpeed = f;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float f) {
        maxLinearAcceleration = f;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float f) {
        maxAngularSpeed = f;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float f) {
        maxAngularAcceleration = f;
    }

}
