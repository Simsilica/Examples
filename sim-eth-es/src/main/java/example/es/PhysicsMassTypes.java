package example.es;

import com.simsilica.es.EntityData;

/**
 * Factory methods for the common object types. Because we run the string names
 * through the EntityData's string index we can't just have normal constants.
 *
 * @author Paul Speed
 */
public class PhysicsMassTypes {

    /**
     * Indicates a normal mass
     */
    public static final String NORMAL = "Normal";

    /**
     * Indicates that the mass is infinite (rate of rotation and translation
     * should not change)
     */
    public static final String INFINITE = "Infinite";

    /**
     * Indicates that the mass's rate of rotation should not change
     */
    public static final String FIXED_ANGULAR_VELOCITY = "Fixed angular velocity";

    /**
     * Indicates that the mass's rate of translation should not change
     */
    public static final String FIXED_LINEAR_VELOCITY = "Fixed linear velocity";

    /**
     * Indicates that the mass is normal, but we want CCD enabled
     */
    public static final String NORMAL_BULLET = "Normal_bullet";

    public static PhysicsMassType normal(EntityData ed) {
        return PhysicsMassType.create(NORMAL, ed);
    }

    public static PhysicsMassType infinite(EntityData ed) {
        return PhysicsMassType.create(INFINITE, ed);
    }

    public static PhysicsMassType fixedAngularVelocity(EntityData ed) {
        return PhysicsMassType.create(FIXED_ANGULAR_VELOCITY, ed);
    }

    public static PhysicsMassType fixedLinearVelocity(EntityData ed) {
        return PhysicsMassType.create(FIXED_LINEAR_VELOCITY, ed);
    }

    public static PhysicsMassType normal_bullet(EntityData ed) {
        return PhysicsMassType.create(NORMAL_BULLET, ed);
    }

}
